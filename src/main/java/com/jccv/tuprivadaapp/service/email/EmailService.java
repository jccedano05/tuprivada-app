package com.jccv.tuprivadaapp.service.email;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import org.mockserver.templates.engine.TemplateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService {


    @Value("${email.sender}")
    private String emailSender;


    private final SesClient sesClient;
    private final SpringTemplateEngine templateEngine;



    @Autowired
    public EmailService(SesClient sesClient, SpringTemplateEngine templateEngine) {
        this.sesClient = sesClient;
        this.templateEngine = templateEngine;
    }

    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        // Procesar template Thymeleaf
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);


        // Construir el correo
        SendEmailRequest request = SendEmailRequest.builder()
                .source("Ayni Comunidad <" +  emailSender +">")  // Correo verificado en SES
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlContent).build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }



}