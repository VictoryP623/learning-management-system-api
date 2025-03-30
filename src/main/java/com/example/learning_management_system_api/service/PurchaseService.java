package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.PurchaseResponseDto;
import com.example.learning_management_system_api.entity.Cart;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Purchase;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.CartRepository;
import com.example.learning_management_system_api.repository.PurchaseRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PurchaseService {

    @Value("${spring.momo.access-key}")
    String ACCESS_KEY;
    @Value("${spring.momo.secret-key}")
    String SECRET_KEY;
    private static final String PARTNER_CODE = "MOMO";
    private static final String ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";

    private final PurchaseRepository purchaseRepository;
    private final CartRepository cartRepository;
    private final StudentRepository studentRepository;
    private final CourseMapper courseMapper;


    public PurchaseService(PurchaseRepository purchaseRepository, CartRepository cartRepository, StudentRepository studentRepository, CourseMapper courseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.cartRepository = cartRepository;
        this.studentRepository = studentRepository;
        this.courseMapper = courseMapper;
    }

    @SneakyThrows
    public Object initPurchase(Long userId) {
        Optional<Student> studentOpt = studentRepository.findByUserId(userId);
        if (studentOpt.isEmpty()) {
            throw new NoSuchElementException("UserId not found or not a student");
        }
        Student student = studentOpt.get();
        List<Cart> cartList = cartRepository.findByStudent(student);
        List<Course> courseList = cartList.stream().map(Cart::getCourse).toList();

        if (courseList.isEmpty()) {
            throw new AppException(400,"Cart is empty");
        }

        Set<Course> courseSet = new HashSet<>(courseList);

        Double totalAmount = courseList.stream().mapToDouble(Course::getPrice).sum();
        Purchase purchase = new Purchase();
        purchase.setStudent(student);
        purchase.setIsPaid(false);
        purchase.setCourses(courseSet);
        purchase.setTotalAmount(totalAmount);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        //Delete cart after add to Purchase
        cartRepository.deleteAll(cartList);

        //Call Momo API
        return initMomoPayment(savedPurchase);
    }

    @SneakyThrows
    public Object initMomoPayment(Purchase purchase) {

        // Payment information
        Integer amount = (int) Math.round(purchase.getTotalAmount());
        String orderId = "LMSPUR_" + purchase.getId() + UUID.randomUUID();
        String orderInfo = "Buy courses in LMS website for user" + purchase.getStudent().getUser().getFullname();
        String redirectUrl = "http://iiex.tech:8080/api/purchases/callback";
        String ipnUrl = "http://iiex.tech:8080/api/purchases/callback";
        String requestType = "payWithMethod";
        String extraData = purchase.getId().toString();
        String orderGroupId = "";
        boolean autoCapture = true;
        String lang = "vi";

        // Generate raw signature string
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount, extraData, ipnUrl, orderId, orderInfo, PARTNER_CODE, redirectUrl, orderId, requestType
        );

        // Generate HMAC SHA256 signature
        String signature = hmacSHA256(rawSignature, SECRET_KEY);

        // Build json request payload
        JSONObject requestBody = new JSONObject();
        requestBody.put("partnerCode", PARTNER_CODE);
        requestBody.put("partnerName", "LMS Website");
        requestBody.put("storeId", "LMS Website");
        requestBody.put("requestId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("lang", lang);
        requestBody.put("requestType", requestType);
        requestBody.put("autoCapture", autoCapture);
        requestBody.put("extraData", extraData);
        requestBody.put("orderGroupId", orderGroupId);
        requestBody.put("signature", signature);

        // Send request to MoMo
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(ENDPOINT);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

        CloseableHttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getCode();
        String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        // Deserialize JSON string into a Map
        ObjectMapper objectMapper = new ObjectMapper();
        Object result = objectMapper.readValue(responseBody, Map.class);
        if (statusCode!=200){
            throw new RuntimeException(result.toString());
        }
        return result;
    }

    public ResponseEntity<String> handleMomoCallback(Map<String, String> params){
        try {
            // Extract parameters from the callback URL
            String partnerCode = params.get("partnerCode");
            String orderId = params.get("orderId");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String payType = params.get("payType");
            String responseTime = params.get("responseTime");
            String extraData = params.get("extraData");
            String receivedSignature = params.get("signature");

            // Build rawSignature
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    ACCESS_KEY, amount, extraData, message, orderId, orderInfo, orderType, partnerCode, payType, requestId, responseTime, resultCode, transId
            );

            // Generate signature
            String generatedSignature = hmacSHA256(rawSignature, SECRET_KEY);

            // Validate signature
            if (generatedSignature.equals(receivedSignature)) {
                //Success
                if ("0".equals(resultCode)) {
                    Purchase purchase = purchaseRepository.findById(Long.valueOf(extraData)).orElseThrow(()-> new NoSuchElementException("User not found"));
                    purchase.setIsPaid(true);
                    purchaseRepository.save(purchase);
                    return new ResponseEntity<>("Purchase successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Payment fail. Result code: "+resultCode,HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>("Invalid signature",HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error:"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SneakyThrows
    String hmacSHA256(String data, String key){
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }

    public List<PurchaseResponseDto> getAllPurchase(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails){
            List<Purchase> listPurchase = purchaseRepository.findDistinctCoursesByStudent_User_IdAndIsPaidTrue(customUserDetails.getUserId());
            return listPurchase.stream().map(this::toDto).toList();
        }
        else return null;
    }

    public PageDto getBoughtCourse(int page, int size){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails){
            List<Purchase> purchaseList = purchaseRepository.findDistinctCoursesByStudent_User_IdAndIsPaidTrue(customUserDetails.getUserId());
            List<Course> boughtCourseEntity = purchaseList.stream()
                    .flatMap(purchase -> purchase.getCourses().stream())
                    .distinct()
                    .toList();
            List<CourseResponseDto> boughtCourse = boughtCourseEntity.stream().map(courseMapper::toResponseDTO).toList();
            if (page < 0 || size < 1) {
                throw new IllegalArgumentException("Page must start from 0 and size must greater than 0");
            }

            int startIndex = page * size;
            if (startIndex >= boughtCourse.size()) {
                return new PageDto(page, size, boughtCourse.size()/size, purchaseList.size(), new ArrayList<>());
            }

            int endIndex = Math.min(startIndex + size, purchaseList.size());
            return new PageDto(page, size, boughtCourse.size()/size, purchaseList.size(),new ArrayList<>(boughtCourse.subList(startIndex, endIndex)));
        }
        else return null;
    }

    PurchaseResponseDto toDto(Purchase purchase){
        List<CourseResponseDto> listCourse = purchase.getCourses().stream().map(courseMapper::toResponseDTO).toList();
        return new PurchaseResponseDto(purchase.getId(), purchase.getTotalAmount(), purchase.getCreatedAt(), listCourse);
    }
}
