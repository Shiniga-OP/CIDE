package com.editor;    

import android.app.Activity;    
import android.graphics.Color;    
import android.graphics.drawable.ColorDrawable;    
import android.text.Editable;    
import android.text.TextWatcher;    
import android.view.Gravity;    
import android.view.View;    
import android.view.inputmethod.InputMethodManager;    
import android.widget.EditText;    
import android.widget.PopupWindow;    
import android.widget.TextView;    
import android.widget.LinearLayout;    
import java.util.ArrayList;    
import java.util.Arrays;    
import java.util.List;    
import android.text.Layout;    
import android.widget.ScrollView;    
import android.view.KeyEvent;    

public class AutoCompletar {    
    public final Activity activity;    
    public final EditText editText;    
    public final String[] palavras;    
    public final List<String> sugestoes = new ArrayList<>();    
    public final PopupWindow popup;    
    public final LinearLayout layout;    
    public static boolean autocomplete = false;

    public AutoCompletar(Activity act, EditText et, String... lista) {    
        activity = act;    
        editText = et;    
        palavras = lista;    
        editText.addTextChangedListener(new olhadorSintaxe(editText));    
        layout = new LinearLayout(activity);    
        layout.setOrientation(LinearLayout.VERTICAL);    
        layout.setBackgroundColor(Color.WHITE);    

        ScrollView sugs = new ScrollView(activity);    
        sugs.addView(layout);    

        popup = new PopupWindow(sugs, 550, 200);    
        popup.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));    
        popup.setOutsideTouchable(true);    
        popup.setFocusable(false);    

        editText.addTextChangedListener(new TextWatcher() {    
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}    
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}    
                @Override public void afterTextChanged(Editable s) {    
                    mostrarSugestoes(obterTokenAtual());    
                }    
            });    
    }    

    public void mostrarSugestoes(String token) {    
        sugestoes.clear();    
        layout.removeAllViews();    
        if(token.length() == 0) {    
            popup.dismiss();    
            return;    
        }    
        List<String> suges = new ArrayList<String>();    
        for(String p : palavras) {    
            if(p.startsWith(token)) suges.add(p);    
        }    

        if(suges.isEmpty()) {    
            popup.dismiss();    
            return;    
        }    

        for(final String s : suges) {    
            TextView t = new TextView(activity);    
            sugestoes.add(s);    
            t.setText(s);    
            t.setPadding(20, 10, 20, 10);    
            t.setTextSize(16);    
            t.setTextColor(Color.BLACK);    
            t.setBackgroundColor(0xFFE0E0E0);    
            t.setOnClickListener(new View.OnClickListener() {    
                    @Override public void onClick(View v) {    
                        substituirToken(s);    
                        popup.dismiss();    
                        editText.requestFocus();    
                        InputMethodManager em = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);    
                        em.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);    
                    }    
                });    
            layout.addView(t);    
        }    

        int antes = editText.getSelectionStart();    
        Layout layout = editText.getLayout();    

        if(layout != null && antes >= 0) {    
            int linha = layout.getLineForOffset(antes);    
            int baselinha = layout.getLineBaseline(linha);    
            int ascent = layout.getLineAscent(linha);    
            float x = layout.getPrimaryHorizontal(antes);    

            int[] loc = new int[2];    
            editText.getLocationOnScreen(loc);    

            int popupX = (int)(loc[0] + x);    
            int popupY = loc[1] + baselinha + ascent;    

            if (!popup.isShowing()) {    
                popup.setWidth(editText.getWidth() / 2);    
                popup.showAtLocation(editText, Gravity.NO_GRAVITY, popupX, popupY);    
            } else {    
                popup.update(popupX, popupY, popup.getWidth(), popup.getHeight());    
            }    
        }    
    }    

    public String obterTokenAtual() {    
        int pos = editText.getSelectionStart();    
        if(pos == 0) return "";    
        CharSequence txt = editText.getText();    
        int i = pos - 1;    
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;    
        return txt.subSequence(i + 1, pos).toString();    
    }    

    public void substituirToken(String nova) {    
        int pos = editText.getSelectionStart();    
        Editable txt = editText.getText();    
        int i = pos - 1;    
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;    
        txt.replace(i + 1, pos, nova);    
        editText.setSelection(i + 1 + nova.length());    
    }    

    public static String[] sintaxe(String linguagem) {    
		if(linguagem.equals("java")) {    
			return new String[]{    
				"System.out.println(", "System.gc()", "Toast.makeText(",    
				"while(", "if(", "for(", "switch(", "try {\n\n\n} catch(Exception e) {\nSystem.out.println(\"erro: \"+e);\n}",     
				".replace(", ".split(", ".trim()", ".toString()", "setContentView(", ".setText(", ".getText(",    

				"public", "private", "protected", "static", "class", "void", "final",    

				"int", "String", "double", "float", "byte", "short", "Integer", "long", "char", "boolean",    

				"import", "package", "new", "return", "case", "break;", "continue;", "else", "this",    

				"@Override", "@JavascriptInterface", "@Deprecated", "@Target", "@TargetApi",    

				"WebView", "EditText", "TextView", "Button", "GLSurfaceView", "SurfaceView",    
				"Canvas", "Toast", "View", "MediaPlayer", "RuntimeException", "Exception",    
				"InputStream", "OutputStream", "PrintStream"    
			};    
		} else if(linguagem.equals("JS")) {    
			return new String[]{    
				".log(", ".error(", ".table(", "fetch(", ".addEventListener(", ".replace(",    
				".trim()", ".split(",  "while(", "for(", "switch(", "if(",     
				".getElementById(", ".forEach(", ".random(", "Math.cos(", "Math.sin(",    

				"let", "const", "var",    

				"class", "static", "function", "import",    

				"=>", "return", "new", "else", "case", "break", "of", "in",    

				"document", "window", "Image", "Audio", "console",    
				"Math"    
			};    
		} else if(linguagem.equals("C")) {    
			return new String[]{    
				"int", "short", "long", "double", "byte",    

				"#include", "struct",    

				"<stdio.h>", "<math.h>",    

				"printf(", "malloc(", "free(", "_Bool"    
			};    
		} else if(linguagem.equals("FP")) {    
			return new String[]{    
				"log(", "se(", "enq(", "por(", "FPexec(",    
				"mAleatorio()", "mPI()", "mCos(", "mSen(", "mAbs(",    
				"lerArquivo(", "gravarArquivo(", "execArquivo(", "obExterno()",    

				"classe", "novo", "este", "senao", "func",    

				"var", "Tex", "Dobro", "Int", "Flutu", "Bool"    
			};    
		} else if(linguagem.equals("asm-arm64")) {    
			return new String[]{    
				"mov", "ldr", "str", "add", "sub", "mul", "udiv", "sdiv", "strb",
				"and", "orr", "eor", "lsl", "lsr", "asr", "cmp", "cmn",    
				"b", "bl", "ret", "cbz", "cbnz", "b.eq", "b.ne", "b.lt", "b.gt",    

				"sp", "pc",    

				"text", "data", "rodata", "bss", "global", "section", "align", "asciz", "word", "byte",    
				"space",    

				"adr", "adrp", "nop", "svc"    
			};    
		}    
		return new String[]{"desconhecida"};    
	}    

    public class olhadorSintaxe implements TextWatcher {    
        public final EditText editor;    
        public boolean processando = false;    
        public int ultimoInicio, ultimoAntes, ultimaQuantidade;    

        public olhadorSintaxe(EditText editor) {    
            this.editor = editor;    
        }    

        @Override    
        public void beforeTextChanged(CharSequence s, int i, int c, int a) { }    

        @Override    
        public void onTextChanged(CharSequence s, int i, int a, int c) {    
            ultimoInicio = i;    
            ultimoAntes = a;    
            ultimaQuantidade = c;    
        }    

        @Override
        public void afterTextChanged(Editable s) {
            if(processando) return;
            processando = true;

            if(ultimoAntes == 0 && ultimaQuantidade == 1) {
                char c = s.charAt(ultimoInicio);

                if(c == '\n') {
                    int pos = ultimoInicio;
                    
                    if(!sugestoes.isEmpty() && autocomplete) {
                        int i = pos - 1;
                        while(i >= 0 && Character.isLetterOrDigit(s.charAt(i))) i--;
                        int tokenStart = i + 1;
                        String nova = sugestoes.get(0);
                        s.replace(tokenStart, pos, nova);
                        pos = tokenStart + nova.length();
                        editor.setSelection(pos + 1);
                        popup.dismiss();
                        sugestoes.clear();
                    }
                    int lin = pos - 1;
                    while(lin >= 0 && s.charAt(lin) != '\n') lin--;
                    int inicioLinha = lin + 1;
                    int contadorTabs = 0;
                    while(inicioLinha + contadorTabs < s.length() && s.charAt(inicioLinha + contadorTabs) == '\t') contadorTabs++;

                    StringBuilder sb = new StringBuilder();
                    for(int i2 = 0; i2 < contadorTabs; i2++) sb.append('\t');

                    int fim = pos - 1;
                    while(fim >= 0 && (s.charAt(fim) == '\t' || s.charAt(fim) == ' ')) fim--;
                    boolean abre = fim >= 0 && (s.charAt(fim) == '{' || s.charAt(fim) == ':');
                    if(abre) sb.append('\t');

                    String indent = sb.toString();
                    s.insert(pos + 1, indent);
                    editor.setSelection(pos + 1 + indent.length());
                } else if(c == '{') {
                    if(ultimoInicio + 1 >= s.length() || s.charAt(ultimoInicio + 1) != '}')
                        s.insert(ultimoInicio + 1, "}");
                    editor.setSelection(ultimoInicio);
                }
            }

            processando = false;
        }    
    }    
}
