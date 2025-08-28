package com.terminal;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.View;

public class Logs extends PrintStream {
    private TextView saida;
    private boolean ativo;

    public Logs(TextView saida) {
        super(new FileOutputStream(FileDescriptor.out));
        this.saida = saida;
        this.ativo = true;
        System.setOut(this);
        System.setErr(this);
    }

    @Override
    public void println(String s) {
        if(saida != null) {
            saida.append(s + "\n");
            rolarProFim();
        }
    }

    @Override
    public void println(Object o) {
        println(String.valueOf(o));
    }

    private void rolarProFim() {
        final ScrollView logs = (ScrollView) saida.getParent();
        logs.post(new Runnable() {
                public void run() {
                    logs.fullScroll(View.FOCUS_DOWN);
                }
            });
    }

    public void parar() {
        if(!ativo) return;
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
        ativo = false;
    }
}
