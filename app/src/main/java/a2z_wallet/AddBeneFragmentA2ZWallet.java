package a2z_wallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.vijayjangid.aadharkyc.R;
import com.vijayjangid.aadharkyc.UserData;
import com.vijayjangid.aadharkyc.activity.AppInProgressActivity;
import com.vijayjangid.aadharkyc.in.RequestHandler;
import com.vijayjangid.aadharkyc.util.APIs;
import com.vijayjangid.aadharkyc.util.AppDialogs;
import com.vijayjangid.aadharkyc.util.InternetConnection;
import com.vijayjangid.aadharkyc.util.MakeToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class AddBeneFragmentA2ZWallet extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = "AddBeneA2ZWallet";
    UserData userData;
    private OnFragmentInteractionListener mListener;
    private String strMobileNumber;
    private HashMap<String, String> hashBankList;

    private EditText ed_beneName;
    private EditText ed_accountNumber;
    private EditText ed_ifsc_code;
    private AutoCompleteTextView atv_bank_name;
    private Button btn_addBeneficiary;
    private ProgressBar progressRR;
    private RelativeLayout rl_progress;
    private Button btn_verifyAccountNo;
    private TextView tv_error_hint;

    private String strBankName;
    private String strAccountNumber;
    private String strIfscCode;
    private String strBeneName;

    private ProgressBar progressBarAccountNo;

    public AddBeneFragmentA2ZWallet() {
        // Required empty public constructor
    }

    public static AddBeneFragmentA2ZWallet newInstance(String strMobileNumber) {
        AddBeneFragmentA2ZWallet fragment = new AddBeneFragmentA2ZWallet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, strMobileNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strMobileNumber = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_bene_fragment_a2z_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {

        userData = new UserData(getContext());

        hashBankList = new HashMap<>();
        ed_beneName = view.findViewById(R.id.ed_beneName);
        ed_accountNumber = view.findViewById(R.id.ed_account_number);
        atv_bank_name = view.findViewById(R.id.atv_bank_name);
        ed_ifsc_code = view.findViewById(R.id.ed_ifsce_code);
        btn_addBeneficiary = view.findViewById(R.id.btn_add_bene);
        tv_error_hint = view.findViewById(R.id.tv_error_hint);
        btn_verifyAccountNo = view.findViewById(R.id.btn_verifyAccountNo);
        progressBarAccountNo = view.findViewById(R.id.progress_accountNo);
        progressRR = view.findViewById(R.id.progressRR);
        rl_progress = view.findViewById(R.id.rl_progress);

        getBankList();
        btn_verifyAccountNo.setOnClickListener(view1 -> {

            if (isFieldFilled()) {
                verifyBankAccount();
            }
        });

        btn_addBeneficiary.setOnClickListener(view1 -> {
            if (InternetConnection.isConnected(getActivity())) {
                if (isFieldFilled()) {
                    strBeneName = ed_beneName.getText().toString();
                    if (!strBeneName.isEmpty()) {
                        addBeneficiary();
                    } else MakeToast.show(getActivity(), "Beneficiary name can't be empty!");
                }
            }
        });

    }

    private void addBeneficiary() {
        progressRR.setVisibility(View.VISIBLE);
        rl_progress.setVisibility(View.VISIBLE);
        btn_addBeneficiary.setVisibility(View.GONE);
        Log.e("addBeneficiary", "" + APIs.BENEFICIARY_ADD_A2ZWallet);
        final StringRequest request = new StringRequest(Request.Method.POST,
                APIs.BENEFICIARY_ADD_A2ZWallet,
                response -> {
                    try {

                        progressRR.setVisibility(View.GONE);
                        rl_progress.setVisibility(View.GONE);
                        btn_addBeneficiary.setVisibility(View.VISIBLE);
                        Log.e("response post", "=" + response.toString());
                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equalsIgnoreCase("35")) {
                            mListener.onFragmentInteraction(4, "BeneficiaryAdded");
                        } else if (status.equalsIgnoreCase("20")) {
                            Dialog dialog = AppDialogs.dialogMessage(getActivity(), message, 0);
                            Button btn_ok = dialog.findViewById(R.id.btn_ok);
                            btn_ok.setOnClickListener(view -> {
                                dialog.dismiss();
                            });
                            dialog.show();
                        }
                        if (status.equalsIgnoreCase("200")) {

                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 0);
                            startActivity(intent);
                        } else if (status.equalsIgnoreCase("300")) {

                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 1);
                            startActivity(intent);
                        } else showConnectionError(message);
                    } catch (JSONException e) {
                        progressRR.setVisibility(View.GONE);
                        rl_progress.setVisibility(View.GONE);
                        btn_addBeneficiary.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressRR.setVisibility(View.GONE);
                    rl_progress.setVisibility(View.GONE);
                    btn_addBeneficiary.setVisibility(View.VISIBLE);
                }) {

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("token", userData.getToken());
                params.put("userId", userData.getId());
                params.put("beneName", ed_beneName.getText().toString());
                params.put("ifscCode", strIfscCode);
                params.put("bankName", strBankName);
                params.put("mobile_number", strMobileNumber);
                params.put("accountNumber", strAccountNumber);
                params.put("caseType", "PreAddBene");
                Log.e("addBeneficiary post", "=" + params.toString());
                return params;
            }
        };
        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private boolean isFieldFilled() {
        strBankName = atv_bank_name.getText().toString();
        strAccountNumber = ed_accountNumber.getText().toString();
        strIfscCode = ed_ifsc_code.getText().toString();

        boolean isFilled = false;
        if (!strBankName.isEmpty()) {
            if (!strAccountNumber.isEmpty()) {
                if (!strIfscCode.isEmpty()) {
                    isFilled = true;
                } else MakeToast.show(getActivity(), "IFSC code can't be empty!");
            } else MakeToast.show(getActivity(), "Account number can't be emtpy!");
        } else MakeToast.show(getActivity(), "Bank Name can't be empty!");
        return isFilled;
    }

    private void getBankList() {

        final StringRequest request = new StringRequest(Request.Method.POST,
                APIs.GET_BANK_LIST_A2ZWallet,
                response -> {
                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.getString("status");
                        if (status.equalsIgnoreCase("1")) {

                            hashBankList.clear();
                            JSONObject jsonObject1 = jsonObject.getJSONObject("bankList");
                            Iterator iterator = jsonObject1.keys();
                            while (iterator.hasNext()) {
                                String key = (String) iterator.next();
                                hashBankList.put(jsonObject1.getString(key), key);

                            }

                            String[] bankList = hashBankList.keySet().toArray(new String[0]);
                            if (getActivity() != null) {
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                        android.R.layout.select_dialog_item, bankList);
                                atv_bank_name.setThreshold(1);
                                atv_bank_name.setAdapter(arrayAdapter);
                                atv_bank_name.setOnItemClickListener((parent, view, position, id) -> {

                                    String value = hashBankList.get(atv_bank_name.getText().toString());
                                    ed_ifsc_code.setText(value);
                                });
                            }


                        } else if (status.equalsIgnoreCase("200")) {
                            String message = jsonObject.getString("message");
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 0);
                            startActivity(intent);
                        } else if (status.equalsIgnoreCase("300")) {
                            String message = jsonObject.getString("message");
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 1);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {

                    }
                },
                error -> {


                }) {

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("token", userData.getToken());
                params.put("userId", userData.getId());

                return params;
            }
        };
        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    private void verifyBankAccount() {
        progressBarAccountNo.setVisibility(View.VISIBLE);
        btn_verifyAccountNo.setVisibility(View.GONE);
        final StringRequest request = new StringRequest(Request.Method.POST,
                APIs.BANK_ACCOUNT_INFO_DMT,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("success")) {
                            String beneName = jsonObject.getString("beneName");
                            ed_beneName.setText(beneName);
                        } else if (status.equalsIgnoreCase("200")) {
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 0);
                            startActivity(intent);
                        } else if (status.equalsIgnoreCase("300")) {
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 1);
                            startActivity(intent);
                        } else if (status.equalsIgnoreCase("pending")) {
                            if (jsonObject.getString("type").equalsIgnoreCase("2")) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Do something
                                        try {
                                            checkStatus(jsonObject.getString("txnId"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 10000);

                                return;
                            } else {
                                progressBarAccountNo.setVisibility(View.GONE);
                                btn_verifyAccountNo.setVisibility(View.VISIBLE);
                                MakeToast.show(getActivity(), message);
                            }
                        } else {
                            MakeToast.show(getActivity(), message);
                        }


                    } catch (JSONException e) {
                        progressBarAccountNo.setVisibility(View.GONE);
                        btn_verifyAccountNo.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBarAccountNo.setVisibility(View.GONE);
                    btn_verifyAccountNo.setVisibility(View.VISIBLE);
                }) {

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("token", userData.getToken());
                params.put("userId", userData.getId());
                params.put("mobile_number", strMobileNumber);
                params.put("bank_account", strAccountNumber);
                params.put("ifsc", strIfscCode);
                params.put("bankCode", strBankName);
                return params;
            }
        };
        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    private void checkStatus(String id) {


        Log.e("CHECK_STATUS_VERI", "=" + APIs.CHECK_STATUS_VERI);
        final StringRequest request = new StringRequest(Request.Method.POST,
                APIs.CHECK_STATUS_VERI,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.e("jsonObject response", "=" + jsonObject.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("msg");

                        Log.e("status=msg", "=" + status + " " + message);
                        if (status.equalsIgnoreCase("1")) {
                            String beneName = jsonObject.getString("beneName");

                            ed_beneName.setText(beneName);
                            Log.e("beneName", "=" + beneName);
                        } else if (status.equalsIgnoreCase("200")) {
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 0);
                            getActivity().startActivity(intent);
                        } else if (status.equalsIgnoreCase("300")) {
                            Intent intent = new Intent(getActivity(), AppInProgressActivity.class);
                            intent.putExtra("message", message);
                            intent.putExtra("type", 1);
                            getActivity().startActivity(intent);
                        } else {
                            Log.e("else status=msg", "=" + status + " " + message);
                            MakeToast.show(getActivity(), message);
                            Dialog dialog = AppDialogs.transactionStatus(getActivity(), message, 2);
                            Button btn_ok = dialog.findViewById(R.id.btn_ok);
                            btn_ok.setOnClickListener(view -> {
                                dialog.dismiss();
                            });
                            dialog.show();
                        }

                        progressBarAccountNo.setVisibility(View.GONE);
                        btn_verifyAccountNo.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        progressBarAccountNo.setVisibility(View.GONE);
                        btn_verifyAccountNo.setVisibility(View.VISIBLE);
                        //  viewHolder.btn_verify.setText("Verified");
                    }
                },
                error -> {
                    progressBarAccountNo.setVisibility(View.GONE);
                    btn_verifyAccountNo.setVisibility(View.VISIBLE);

                }) {

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("token", userData.getToken());
                params.put("userId", userData.getId());
                params.put("id", id);

                Log.e("check status", "=" + params.toString());
                return params;
            }
        };
        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }


    private void showConnectionError(String message) {
        tv_error_hint.setText(message);
        tv_error_hint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int type, String data);
    }
}
