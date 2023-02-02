package tim5.psp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tim5.psp.model.PaymentMethod;
import tim5.psp.model.Subscription;
import tim5.psp.model.TokenUtils;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Service
public class TokenService {
    @Value("secret")
    public String SECRET;
    @Autowired
    private TokenUtils tokenUtils;

    public String generateTokenPayment(Subscription subscription){
        return tokenUtils.generateToken(subscription.getWebShopURI(), getPermissions(subscription));
    }

    private String getPermissions(Subscription subscription){
        String permission = "";
        for (PaymentMethod m : subscription.getMethods()) {
            permission += m.getMethodName() + ",";
        }
        permission += "transaction_permission";
        return permission;
    }

    public Boolean validateToken(String token, String permission) throws JSONException {
        //  TODO: time validation
        String permissions = decodeJWTToken(token);
        String[] permissionArray = permissions.split(",");
        System.out.println(permissions);
        System.out.println(permission);
        for(String permissionFromArray:permissionArray)
            if(permissionFromArray.equals(permission))
                return true;

        return false;
    }
    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    public String decodeJWTToken(String token) throws JSONException {
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String[] parts = token.split("\\.");

        JSONObject header = new JSONObject(decode(parts[0]));
        JSONObject payload = new JSONObject(decode(parts[1]));



        return   payload.get("permissions").toString();
    }

    public String decodeJWTToken(String token, String secretKey) throws Exception {
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String[] chunks = token.split("\\.");

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        String tokenWithoutSignature = chunks[0] + "." + chunks[1];
        String signature = chunks[2];

        SignatureAlgorithm sa = HS256;
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), sa.getJcaName());

        DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(sa, secretKeySpec);

        if (!validator.isValid(tokenWithoutSignature, signature)) {
            throw new Exception("Could not verify JWT token integrity!");
        }

        return header + " " + payload;
    }
}
