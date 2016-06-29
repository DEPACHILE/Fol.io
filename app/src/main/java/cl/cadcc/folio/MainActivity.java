package cl.cadcc.folio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cl.cadcc.folio.api.ApiHttps;
import cl.cadcc.folio.fragments.ServerConnectFragment;
import cl.cadcc.folio.fragments.WelcomeFragment;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;

public class MainActivity extends AppCompatActivity implements WelcomeFragment.OnFragmentInteractionListener {

    private BroadcastReceiver nfcChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment welcomeFragment = new WelcomeFragment();
        setContentView(R.layout.activity_main);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        t.replace(R.id.folio_fragment,welcomeFragment , "welcome");
        t.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startNfcDetector();
        startNfcReader();

        onNfcDetectorChange();
    }

    @Override
    protected void onPause() {
        stopNfcReader();
        stopNfcDetector();

        super.onPause();
    }

    private void onNfcDetectorChange() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
        TextView tuiTextView = (TextView) findViewById(R.id.textView_tui);

        if (tuiTextView == null) return;

        if (nfcAdapter == null) {
            tuiTextView.setText("Lo sentimos, tu celular no tiene NFC.");
        } else if (!nfcAdapter.isEnabled()) {
            tuiTextView.setText("NFC está deshabilitado, habilítalo, por favor.");
        } else {
            tuiTextView.setText("Acerca la tarjeta, por favor.");
        }
    }

    private void startNfcDetector() {
        nfcChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onNfcDetectorChange();
            }
        };
        registerReceiver(nfcChangedReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
    }

    public void stopNfcDetector() {
        unregisterReceiver(nfcChangedReceiver);
    }

    public void startNfcReader() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) return;

        int flags = NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK | NfcAdapter.FLAG_READER_NFC_A;

        nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                final String cardId = bytesToHexString(tag.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment serverConnectFragment = new ServerConnectFragment();
                        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                        t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        t.replace(R.id.folio_fragment,serverConnectFragment , "connect");
                        t.commit();
                        getSupportFragmentManager().executePendingTransactions();
                        TextView nfcValue = (TextView) findViewById(R.id.nfc_value);
                        if (nfcValue == null) {
                            Log.d("Adderou", "Es nulo!");
                            return;
                        } else {
                            nfcValue.setText("Card ID: " + cardId.substring(2, cardId.length()).toUpperCase());
                            String tuiId=cardId.substring(2, cardId.length()).toUpperCase();
                            try {
                                Log.d("testTUID",tuiId);
                                findUser(tuiId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }, flags, null);
    }


    public void findUser(String tuiId) throws JSONException {
        ApiHttps.get("/vote/1/1/"+tuiId+"/1", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("testAPISUCCESS",response.toString());
                Toast.makeText(getApplicationContext(),response.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                JSONObject firstEvent = null;
                String tweetText = null;
                try {
                    firstEvent = (JSONObject) timeline.get(0);
                    tweetText = firstEvent.getString("text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // Do something with the response
                Log.d("testAPI",tweetText);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("testAPI","Error");
            }
        });
    }

    public void stopNfcReader() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) return;
        nfcAdapter.disableReaderMode(this);
    }

    private String bytesToHexString(byte[] src) {

        if (src == null || src.length <= 0) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder("0x");

        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
            stringBuilder.append(buffer);
        }

        return stringBuilder.toString();
    }
}
