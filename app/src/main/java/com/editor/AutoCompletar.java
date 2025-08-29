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
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.Button;
import android.widget.Toast;

public class AutoCompletar {    
    public final Activity activity;    
    public final EditText editText;    
    public final String[] palavras;    
    public final List<String> sugestoes = new ArrayList<>();    
    public final PopupWindow popup;    
    public final LinearLayout layout;    
    public static boolean autocomplete = false;
    public final HashSet<String> palavrasDoc = new HashSet<>();

    public AutoCompletar(Activity act, EditText et, String... lista) {    
        activity = act;    
        editText = et;    
        palavras = lista;    

        for(String palavra : palavras) palavrasDoc.add(palavra);

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
					palavrasDoc.clear();
					coletarPalavrasDoDoc(s.toString());
                    mostrarSugestoes(obterTokenAtual());    
                }    
            });    
    }

    public void coletarPalavrasDoDoc(String texto) {
        Pattern p = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher m = p.matcher(texto);

        while(m.find()) {
            String palavra = m.group();
            if(!palavrasDoc.contains(palavra) && palavra.length() > 1) palavrasDoc.add(palavra);
        }
    }

    public void mostrarSugestoes(String token) {    
        sugestoes.clear();    
        layout.removeAllViews();    
        if(token.length() == 0) {    
            popup.dismiss();    
            return;    
        }    
        List<String> suges = new ArrayList<String>();    
        for(String p : palavrasDoc) {    
            if(p.toLowerCase().startsWith(token.toLowerCase())) suges.add(p);    
        }
        if(suges.isEmpty()) {
            for(String p : palavras) {    
                if(p.toLowerCase().startsWith(token.toLowerCase())) suges.add(p);    
            }
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

            if(!popup.isShowing()) {    
                popup.setWidth(editText.getWidth() / 2);    
                popup.showAtLocation(editText, Gravity.NO_GRAVITY, popupX, popupY);    
            } else popup.update(popupX, popupY, popup.getWidth(), popup.getHeight());    
        }    
    }    

    public String obterTokenAtual() {
        int pos = editText.getSelectionStart();
        if(pos == 0) return "";
        CharSequence txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
              txt.charAt(i) == '\n'
        )) i--;
        return txt.subSequence(i + 1, pos).toString();
    }    

    public void substituirToken(String nova) {    
        int pos = editText.getSelectionStart();    
        Editable txt = editText.getText();    
        int i = pos - 1;    
        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
              txt.charAt(i) == '\n'
        )) i--;
        txt.replace(i + 1, pos, nova);    
        editText.setSelection(i + 1 + nova.length());    
    }    
	
	public void mostrarBuscaRapida() {
		try {
			LinearLayout layoutBusca = new LinearLayout(activity);
			layoutBusca.setOrientation(LinearLayout.VERTICAL);
			layoutBusca.setPadding(20, 20, 20, 20);

			final EditText campoBusca = new EditText(activity);
			campoBusca.setHint("Buscar...");

			final EditText campoSubstituir = new EditText(activity);
			campoSubstituir.setHint("Substituir por...");

			Button btnBuscar = new Button(activity);
			btnBuscar.setText("Buscar");

			Button btnSubstituir = new Button(activity);
			btnSubstituir.setText("Substituir");

			Button btnSubstituirTudo = new Button(activity);
			btnSubstituirTudo.setText("Substituir Tudo");

			layoutBusca.addView(campoBusca);
			layoutBusca.addView(campoSubstituir);
			layoutBusca.addView(btnBuscar);
			layoutBusca.addView(btnSubstituir);
			layoutBusca.addView(btnSubstituirTudo);

			final PopupWindow popupBusca = new PopupWindow(layoutBusca, 600, 400);
			popupBusca.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			popupBusca.setOutsideTouchable(true);
			popupBusca.setFocusable(true);

			int[] local = new int[2];
			editText.getLocationOnScreen(local);
			popupBusca.showAtLocation(editText, Gravity.TOP, local[0], local[1] + 50);

			btnBuscar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						if(!textoBusca.isEmpty()) {
							int pos = editText.getText().toString().indexOf(textoBusca);
							if(pos != -1) editText.setSelection(pos, pos + textoBusca.length());
						}
					}
				});

			btnSubstituir.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						String textoSubstituir = campoSubstituir.getText().toString();

						if(!textoBusca.isEmpty()) {
							Editable texto = editText.getText();
							int i = editText.getSelectionStart();
							int e = editText.getSelectionEnd();
							if(i != e) {
								String selecionado = texto.subSequence(i, e).toString();
								if(selecionado.equals(textoBusca)) texto.replace(i, e, textoSubstituir);
							}
						}
					}
				});

			btnSubstituirTudo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						String textoSubstituir = campoSubstituir.getText().toString();

						if(!textoBusca.isEmpty()) {
							String textoCompleto = editText.getText().toString();
							String novoTxt = textoCompleto.replace(textoBusca, textoSubstituir);
							editText.setText(novoTxt);
						}
					}
				});
		} catch(Exception e) {
			Toast.makeText(activity, "erro: "+e, Toast.LENGTH_SHORT).show();
		}
	}

    public static String[] sintaxe(String linguagem) {    
        if(linguagem.equals("java")) {    
            return new String[]{    
                "System.out.println(", "System.gc();", "Toast.makeText(",    
                "while(", "if(", "for(", "switch(", "try {\n\n\n} catch(Exception e) {\nSystem.out.println(\"erro: \"+e);\n}",     
                ".replace(", ".split(", ".trim()", ".toString()", "setContentView(", ".setText(", ".getText()",    

                "public", "private", "protected", "static", "class", "void", "final",    

                "int", "String", "double", "float", "byte", "short", "Integer", "long", "char", "boolean",    
                "final",

                "import", "package", "new", "return", "case", "break;", "continue;", "else", "this",    

                "@Override", "@JavascriptInterface", "@Deprecated", "@Target", "@TargetApi",    

                "WebView", "EditText", "TextView", "Button", "GLSurfaceView", "SurfaceView",    
                "Canvas", "Toast", "View", "MediaPlayer", "RuntimeException", "Exception",    
                "InputStream", "OutputStream", "PrintStream"    
            };    
        } else if(linguagem.equals("JS")) {    
            return new String[]{    
                "console.log(", "console.error(", "console.table(", "fetch(", ".addEventListener(", ".replace(",    
                ".trim()", ".split(",  "while(", "for(", "switch(", "if(",     
                ".getElementById(", ".forEach(", "Math.random(", "Math.cos(", "Math.sin(",    

                "let", "const", "var",    

                "class", "static", "function", "import",    

                "=>", "return", "new", "else", "case", "break", "of", "in",    

                "document", "window", "Image", "Audio", "console",    
                "Math"    
            };    
        } else if(linguagem.equals("C")) {    
            return new String[]{    
                "int", "short", "long", "double", "byte", "size_t", "const",
                "bool",

                "#include", "#define", "#ifndef", "#undef", "#endif",
                "#if", "#else",

                "<stdio.h>", "<math.h>", "<memory.h>", "<string.h>",
                "<stdlib.h>", "<ctype.h>",

                "struct", "typedef", "enum", "static",

                "printf(", "malloc(", "free(",

                "if(", "while(", "for(", "switch(", "case", "break;", "else"
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

                ".text", ".data", ".rodata", ".bss", ".global", ".section", ".align", ".asciz", ".word", ".byte",    
                ".space",    

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
        public void afterTextChanged(Editable txt) {
            if(processando) return;
            processando = true;

            if(ultimoAntes == 0 && ultimaQuantidade == 1) {
                char c = txt.charAt(ultimoInicio);
				int pos = ultimoInicio;
				if(pos >= 6 && txt.subSequence(pos-6, pos+1).toString().equals("//busca")) {
					txt.replace(pos-6, pos+1, "");
					mostrarBuscaRapida();
				}
                if(c == '\n') {
                    if(!sugestoes.isEmpty() && autocomplete) {
                        int i = pos - 1;
                        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
                              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
                              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
                              txt.charAt(i) == '\n'
                        )) i--;
                        int tokenI = i + 1;
                        String nova = sugestoes.get(0);
                        txt.replace(tokenI, pos, nova);
                        pos = tokenI + nova.length();
                        editor.setSelection(pos + 1);
                        popup.dismiss();
                        sugestoes.clear();
                    }
                    int lin = pos - 1;
                    while(lin >= 0 && txt.charAt(lin) != '\n') lin--;
                    int inicioLinha = lin + 1;
                    int contadorTabs = 0;
                    while(inicioLinha + contadorTabs < txt.length() && txt.charAt(inicioLinha + contadorTabs) == '\t') contadorTabs++;

                    StringBuilder sb = new StringBuilder();
                    for(int i2 = 0; i2 < contadorTabs; i2++) sb.append('\t');

                    int fim = pos - 1;
                    while(fim >= 0 && (txt.charAt(fim) == '\t' || txt.charAt(fim) == ' ')) fim--;
                    boolean abre = fim >= 0 && (txt.charAt(fim) == '{' || txt.charAt(fim) == ':');
                    if(abre) sb.append('\t');

                    String inden = sb.toString();
                    txt.insert(pos + 1, inden);
                    editor.setSelection(pos + 1 + inden.length());
                } else if(c == '{') {
                    if(ultimoInicio + 1 >= txt.length() || txt.charAt(ultimoInicio + 1) != '}')
                        txt.insert(ultimoInicio + 1, "}");
                    editor.setSelection(ultimoInicio);
                }
            }
            processando = false;
        }    
    }    
}
