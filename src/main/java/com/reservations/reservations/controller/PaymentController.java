package com.reservations.reservations.controller;

import com.reservations.reservations.model.Reservation;
import com.reservations.reservations.service.PaymentService;
import com.reservations.reservations.service.ReservationService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.security.Principal;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Value("${stripe.secret}")
    private String stripeSecret;

    @Value("${app.base-url}")
    private String baseUrl; // http://localhost:8080

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    public PaymentController(ReservationService reservationService, PaymentService paymentService) {
        this.reservationService = reservationService;
        this.paymentService = paymentService;
    }

    @PostConstruct
    public void init() {
        if (stripeSecret == null || !stripeSecret.startsWith("sk_")) {
            throw new IllegalStateException("Stripe secret key manquante ou invalide");
        }
        Stripe.apiKey = stripeSecret.trim();
    }

    /** Démarre le checkout et redirige l'utilisateur vers Stripe */
    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> checkout(@RequestParam("reservationId") Long reservationId, Principal principal) throws Exception {
        // Récupérer la réservation et valider qu’elle est à l’utilisateur + PENDING
        Reservation r = reservationService.getOwnedPendingReservation(reservationId, principal.getName());

        long amount = paymentService.amountInCents(r);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/payments/success?session_id={CHECKOUT_SESSION_ID}&rid=" + r.getId())
                .setCancelUrl(baseUrl + "/payments/cancel?rid=" + r.getId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("eur")
                                        .setUnitAmount(amount) // en cents
                                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Réservation #" + r.getId() + " – " + r.getRepresentation().getShow().getTitle())
                                                .build())
                                        .build())
                                .build())
                .putMetadata("reservationId", String.valueOf(r.getId())) // utile si tu utilises aussi un webhook
                .build();


        Session session = Session.create(params);

        // Redirection 303 vers Stripe
        return ResponseEntity.status(303).location(URI.create(session.getUrl())).build();
    }

    /** Redirigé ici après paiement OK (côté user). Le webhook confirmera le statut PAID. */
    @GetMapping("/success")
    public String success(@RequestParam("session_id") String sessionId,
                          @RequestParam("rid") Long rid,
                          RedirectAttributes ra) {
        try {
            // 1) Vérifier auprès de Stripe l’état réel de la session
            Session session = Session.retrieve(sessionId);

            boolean ok = "complete".equalsIgnoreCase(session.getStatus())
                    && "paid".equalsIgnoreCase(session.getPaymentStatus());

            if (ok) {
                // 2) (Option) recouper avec la metadata
                String mdRid = session.getMetadata() != null ? session.getMetadata().get("reservationId") : null;
                Long reservationId = (mdRid != null) ? Long.valueOf(mdRid) : rid;

                // 3) Marquer payé en base
                reservationService.markPaid(reservationId);

                ra.addFlashAttribute("message", "Paiement confirmé ✅");
            } else {
                ra.addFlashAttribute("error",
                        "Paiement non confirmé (" + session.getStatus() + "/" + session.getPaymentStatus() + ")");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de vérifier le paiement.");
        }
        return "redirect:/profile";
    }


    /** Redirigé ici si l'utilisateur annule/ferme le checkout */
    @GetMapping("/cancel")
    public String cancel(@RequestParam("rid") Long rid, RedirectAttributes ra) {
        ra.addFlashAttribute("message", "Paiement annulé.");
        return "redirect:/profile";
    }

    /** Webhook Stripe: confirmation asynchrone checkout.session.completed -> marque la réservation comme PAID */
    @PostMapping("/webhook")
    @ResponseBody
    public String webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        //  Pour un POC: on lit simplement le JSON; en prod: vérifier la signature avec l’endpoint secret
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
            String type = json.get("type").getAsString();
            if ("checkout.session.completed".equals(type)) {
                var sessionObj = json.getAsJsonObject("data").getAsJsonObject("object");
                var metadata = sessionObj.getAsJsonObject("metadata");
                if (metadata != null && metadata.has("reservationId")) {
                    Long rid = metadata.get("reservationId").getAsLong();
                    reservationService.markPaid(rid);
                }
            }
            return "ok";
        } catch (Exception e) {
            return "ignored";
        }
    }
}

