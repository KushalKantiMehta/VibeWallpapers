package kushal.droidlab.vibewallpapers;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountFragment extends Fragment {

    private static final int GOOGLE_SiGN_IN_CODE =212;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            return inflater.inflate(R.layout.account_not_logged_in, container, false);
        }

            return inflater.inflate(R.layout.fragment_account, container, false);



    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(),gso);


        if(FirebaseAuth.getInstance().getCurrentUser() != null){

            CircleImageView imageview = view.findViewById(R.id.profileimage);
            TextView username = view.findViewById(R.id.username);
            TextView user_email =view.findViewById(R.id.email_username);

            FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();

            Glide.with(requireActivity())
                    .load(Objects.requireNonNull(user.getPhotoUrl()).toString())
                    .into(imageview);

            username.setText(user.getDisplayName());
            user_email.setText(user.getEmail());

            view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.content_fragment, new AccountFragment())
                                    .commit();
                        }
                    });

                }
            });

        }
        else{
            view.findViewById(R.id.google_signin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(intent,GOOGLE_SiGN_IN_CODE);
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SiGN_IN_CODE){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(),"Log in Successfully",Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, new AccountFragment()).commit();
                }
                else{
                    Toast.makeText(getActivity(),"Log in Failed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}