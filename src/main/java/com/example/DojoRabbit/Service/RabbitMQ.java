package com.example.DojoRabbit.Service;


import com.example.DojoRabbit.data.Notification;
import com.example.DojoRabbit.serializer.JSONMapper;
import com.example.DojoRabbit.serializer.JSONMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RabbitMQ {
    public static final String EVENTS_QUEUE = "events.queue";
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQ.class);
    private final JSONMapper mapper = new JSONMapperImpl();
    @Autowired
    private JavaMailSender javaMailSender;

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    @RabbitListener(queues = EVENTS_QUEUE)
    public void recievedMessage(String message) {
        Notification notification = Notification.from(message);

        if(notification.getType().equals("co.com.retoca.model.paciente.events.CitaAgregada")){

            Pattern FechaCita =Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
            Pattern CorreoNotificar = Pattern.compile("\"correo\":\"(\\S+?)\"");
            Pattern HoraNotificar = Pattern.compile("\"hora\":\"(\\S+?)\"");
            Pattern CCNotificar = Pattern.compile("\"aggregateRootId\":\"(\\S+?)\"");

            Matcher matcherfecha = FechaCita.matcher(notification.getBody());
            Matcher matcherCorreo = CorreoNotificar.matcher(notification.getBody());
            Matcher matcherHora = HoraNotificar.matcher(notification.getBody());
            Matcher matcherCC = CCNotificar.matcher(notification.getBody());

            matcherfecha.find();
            matcherCorreo.find();
            matcherHora.find();
            matcherCC.find();

                String fecha = matcherfecha.group(1);
                String Correo = matcherCorreo.group(1);
                String Hora = matcherHora.group(1);
                String CC = matcherCC.group(1);

            try {

                mailMessage.setTo(Correo);
                mailMessage.setSubject("Confirmación de cita con el Doctor Julián Mazo");
                mailMessage.setText("Este Correo es para confirmar la asignación de cita del paciente con cédula: "+
                        CC+"\n El día: "+fecha+" a las "+Hora+". Llegar 15 minutos antes. ");

                javaMailSender.send(mailMessage);
            } catch (Exception e) {
                LOGGER.error("Error al enviar el correo electrónico: " + e.getMessage());
            }


        }else {
            LOGGER.info("mensaje recibido de TIPO: "+notification);

        }


    }
}
