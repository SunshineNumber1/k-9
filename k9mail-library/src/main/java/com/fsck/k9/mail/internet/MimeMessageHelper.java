package com.fsck.k9.mail.internet;


import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.FancyPart;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Multipart;
import com.fsck.k9.mail.Part;
import org.apache.james.mime4j.util.MimeUtil;


public class MimeMessageHelper {
    private MimeMessageHelper() {
    }

    public static void setBody(Part part, Body body) throws MessagingException {
        part.setBody(body);

        if (part instanceof Message) {
            part.setHeader("MIME-Version", "1.0");
        }

        if (body instanceof Multipart) {
            Multipart multipart = ((Multipart) body);
            multipart.setParent(part);
            String mimeType = multipart.getMimeType();
            String contentType = String.format("%s; boundary=\"%s\"", mimeType, multipart.getBoundary());
            part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);
            if (MimeUtility.isSameMimeType(mimeType, "multipart/signed")) {
                setEncoding(part, MimeUtil.ENC_7BIT);
            } else {
                setEncoding(part, MimeUtil.ENC_8BIT);
            }
        } else if (body instanceof TextBody) {
            FancyPart fancyPart = FancyPart.from(part);
            String contentType;
            if (fancyPart.isMatchingmimeType("text/*")) {
                contentType = String.format("%s;\r\n charset=utf-8", fancyPart.getMimeType());
                String name = fancyPart.getContentTypeName();
                if (name != null) {
                    contentType += String.format(";\r\n name=\"%s\"", name);
                }
            } else {
                contentType = fancyPart.getMimeType();
                if (contentType == null) {
                    contentType = "text/plain";
                }
            }
            part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);

            setEncoding(part, MimeUtil.ENC_8BIT);
        }
    }

    public static void setEncoding(Part part, String encoding) throws MessagingException {
        Body body = part.getBody();
        if (body != null) {
            body.setEncoding(encoding);
        }
        part.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, encoding);
    }
}
