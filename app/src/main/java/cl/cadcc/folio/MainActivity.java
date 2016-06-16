package cl.cadcc.folio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver nfcChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String cardId = bytesToHexString(tag.getId());
            Toast.makeText(this, cardId, Toast.LENGTH_LONG).show();
        }
    }

    private void onNfcDetectorChange() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
        TextView tuiTextView = (TextView) findViewById(R.id.textView_tui);

        if (tuiTextView == null) return;

        if (nfcAdapter == null) {
            tuiTextView.setText("NFC is not available");
        } else if (!nfcAdapter.isEnabled()) {
            tuiTextView.setText("NFC is disabled");
        } else {
            tuiTextView.setText("Waiting to read");
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

        Intent i = new Intent(this, this.getClass());
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0, i, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        ndef.addCategory(Intent.CATEGORY_DEFAULT);

        IntentFilter[] intentFilters = new IntentFilter[]{ndef};

        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, intentFilters, null);
    }

    public void stopNfcReader() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) return;
        nfcAdapter.disableForegroundDispatch(this);
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
