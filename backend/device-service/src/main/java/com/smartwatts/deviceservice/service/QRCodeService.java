package com.smartwatts.deviceservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QRCodeService {
    
    private static final String ONBOARDING_BASE_URL = "https://onboard.mysmartwatts.com/register";
    
    /**
     * Generate QR code for partner onboarding
     * @param partnerId The unique partner identifier
     * @return Base64 encoded QR code image
     */
    public String generatePartnerQRCode(String partnerId) {
        try {
            String qrCodeData = ONBOARDING_BASE_URL + "?partner=" + partnerId;
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 300, 300);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    /**
     * Generate QR code URL for partner
     * @param partnerId The unique partner identifier
     * @return The onboarding URL with partner parameter
     */
    public String generatePartnerQRCodeUrl(String partnerId) {
        return ONBOARDING_BASE_URL + "?partner=" + partnerId;
    }
    
    /**
     * Extract partner ID from QR code data
     * @param qrCodeData The QR code data
     * @return Partner ID or null if invalid
     */
    public String extractPartnerIdFromQRCode(String qrCodeData) {
        if (qrCodeData == null || !qrCodeData.contains("partner=")) {
            return null;
        }
        
        try {
            String[] parts = qrCodeData.split("partner=");
            if (parts.length > 1) {
                return parts[1].split("&")[0]; // Remove any additional parameters
            }
        } catch (Exception e) {
            // Invalid QR code data
        }
        
        return null;
    }
    
    /**
     * Validate QR code data format
     * @param qrCodeData The QR code data to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidQRCodeData(String qrCodeData) {
        return qrCodeData != null && 
               qrCodeData.startsWith(ONBOARDING_BASE_URL) && 
               qrCodeData.contains("partner=");
    }
} 