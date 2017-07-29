package kdas.i_nterface.fetch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignIn extends AppCompatActivity {

    String phone, name, mVerificationId;

    FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    FloatingActionButton signup, done;
    String code;

    EditText contact, name_ed, code_ed;
    TextInputLayout code_til, contact_til, name_til;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        contact = (EditText) findViewById(R.id.contact);
        name_ed = (EditText) findViewById(R.id.name);
        code_ed = (EditText) findViewById(R.id.code);

        contact_til = (TextInputLayout)findViewById(R.id.contact_til);
        code_til = (TextInputLayout)findViewById(R.id.code_til);
        name_til = (TextInputLayout)findViewById(R.id.textInputLayout_name);

        phone = contact.getText().toString();
        name = name_ed.getText().toString();

        signup = (FloatingActionButton) findViewById(R.id.signup);
        done = (FloatingActionButton) findViewById(R.id.donefab);

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("log", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("log", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("log", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!contact.getText().toString().isEmpty()) {
                    initVerification(contact.getText().toString());

                    contact_til.setVisibility(View.GONE);
                    name_til.setVisibility(View.GONE);
                    code_til.setVisibility(View.VISIBLE);
                }
                else
                    Toast.makeText(getApplicationContext(), "Cant be empty", Toast.LENGTH_LONG).show();

            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!code_ed.getText().toString().isEmpty()){
                    code = code_ed.getText().toString();
                    verifyCode(mVerificationId, code);
                }
                else
                    Toast.makeText(getApplicationContext(), "Cant be empty", Toast.LENGTH_LONG).show();

            }
        });

    }

    private void verifyCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void initVerification(String num){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                num,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("log", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("log", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignIn.this, MainActivity.class));
            finish();
        }
    }
}
