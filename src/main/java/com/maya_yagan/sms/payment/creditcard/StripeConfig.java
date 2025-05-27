package com.maya_yagan.sms.payment.creditcard;

import com.stripe.Stripe;
import io.github.cdimascio.dotenv.Dotenv;

public class StripeConfig {
    public static void init() {
        Dotenv dotenv = Dotenv.load();
        Stripe.apiKey = dotenv.get("STRIPE_SECRET_KEY");
    }
}
