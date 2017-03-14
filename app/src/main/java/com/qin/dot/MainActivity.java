package com.qin.dot;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(new Playground(this));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN))
        {
            System.out.println("exit");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("information")
                    .setMessage("退出？")
                    .setIcon(R.drawable.idea)
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                            Uri uri= Uri.parse("https://canhetingsky.github.io/");
                            Intent intent =new Intent(Intent.ACTION_VIEW,uri);
                            startActivity(intent);
                        }
                    }).setNegativeButton("否",null)
                    .create()
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
