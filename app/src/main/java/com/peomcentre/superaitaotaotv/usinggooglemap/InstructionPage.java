package com.peomcentre.superaitaotaotv.usinggooglemap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by superaitaotaoTV on 16/11/14.
 */
public class InstructionPage extends Activity {
    private TextView textView;
    private String instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction_page);
        textView = (TextView)findViewById(R.id.instruction_text_view);
        instructions = getIntent().getStringExtra("manySegments");
        textView.setText(instructions);
    }


}
