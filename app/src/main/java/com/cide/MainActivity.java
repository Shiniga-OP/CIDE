package com.cide;
 
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import com.editor.AutoCompletar;
import com.editor.Sintaxe;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Button;
import java.io.File;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.net.Uri;
import java.util.HashMap;
import com.terminal.TerminalActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends Activity {
     public EditText editor, nomeArquivo;
     public ListView pastas;
     public String arquivoAtual;
     public ImageView salvar;
     public File dirTrabalho;
     public List<String> projetos = new ArrayList<>();
    public List<Map<String, Object>> projetosLista = new ArrayList<>();
     
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.ide);
        pastas = findViewById(R.id.pastas);
        editor = findViewById(R.id.editor);
        nomeArquivo = findViewById(R.id.nomeArquivo);
        salvar = findViewById(R.id.salvar);
        
        dirTrabalho = new File(getFilesDir()+"/CASA");
        if(!dirTrabalho.exists()) dirTrabalho.mkdir();
        
        new Sintaxe.C().aplicar(editor);
        new AutoCompletar(this, editor, AutoCompletar.sintaxe("C"));
        
        pastas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?>  param1, View param2, int param3, long param4) {
                    File pasta = new File(projetos.get(param3));
                    if(pasta.isDirectory()) {
                        dirTrabalho = pasta;
                        _capturar_pasta();
                    } else {
                        editor.setText(lerArq(projetos.get(param3)));
                        arquivoAtual = projetos.get(param3);
                        String[] as = arquivoAtual.split("/");
                        nomeArquivo.setText(as[as.length-1]);
                        _capturar_pasta();
                    }
                }
            });
        pastas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> p, View view, int pos, long id) {
                    mostrarMenuContexto(projetos.get(pos));
                    return true;
                }
            });
        salvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String caminho;
                        if(nomeArquivo.getText().toString().startsWith("/"))  caminho = nomeArquivo.getText().toString();
                        else caminho = dirTrabalho.getAbsolutePath() + "/" + nomeArquivo.getText().toString();

                        if(caminho.endsWith("/")) {
							File pasta = new File(caminho);
							if(!pasta.exists()) pasta.mkdirs();
						} else escreverArq(caminho, editor.getText().toString());
                        arquivoAtual = caminho;

                        Toast.makeText(getApplicationContext(), "arquivo salvo", Toast.LENGTH_SHORT).show();
                        _capturar_pasta();
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "erro: "+e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            _capturar_pasta();
    }
    
    public void mostrarMenuContexto(final String caminho) {
        final File arquivo = new File(caminho);

        final CharSequence[] opcoes = new CharSequence[]{"Renomear", "Excluir"};

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(arquivo.getName());
        b.setItems(opcoes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int i) {
                    switch(i) {
                        case 0:
                            renomearArquivo(arquivo);
                            break;
                        case 1:
                            excluirArquivo(arquivo);
                            break;
                    }
                }
            });
        b.show();
    }

    public void renomearArquivo(final File arquivo) {
        final EditText e = new EditText(this);
        e.setText(arquivo.getName());

        new AlertDialog.Builder(this)
            .setTitle("Renomear")
            .setView(e)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int i) {
                    String novoNome = e.getText().toString();
                    File novoArquivo = new File(arquivo.getParent(), novoNome);
                    if(arquivo.renameTo(novoArquivo)) _capturar_pasta();
                    else Toast.makeText(MainActivity.this, "Falha ao renomear", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    public void excluirArquivo(final File arquivo) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir")
            .setMessage("Tem certeza que deseja excluir " + arquivo.getName() + "?")
            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int i) {
                    if(deletarRecursivo(arquivo)) _capturar_pasta();
                    else Toast.makeText(MainActivity.this, "Falha ao excluir", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Não", null)
            .show();
    }

    private boolean deletarRecursivo(File arquivo) {
        if(arquivo.isDirectory()) for(File filho : arquivo.listFiles()) deletarRecursivo(filho);
        return arquivo.delete();
    }
    
    public static void listeDir(String cam, List<String> lista) {
        File dir = new File(cam);
        if(!dir.exists() || dir.isFile()) return;

        File[] listeArqs = dir.listFiles();
        if(listeArqs == null || listeArqs.length <= 0) return;

        if(lista == null) return;
        lista.clear();
        for(File arq : listeArqs) {
            lista.add(arq.getAbsolutePath());
        }
    }

    public static boolean arqExiste(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists();
    }

    public static void criarDir(String caminho) {
        if(!arqExiste(caminho)) {
            File file = new File(caminho);
            file.mkdirs();
        }
    }

    public static void criarArq(String caminho) {
        int ultimoPasso= caminho.lastIndexOf(File.separator);
        if(ultimoPasso > 0) {
            String caminhoDiretorio = caminho.substring(0, ultimoPasso);
            criarDir(caminhoDiretorio);
        }
        File arquivo = new File(caminho);
        try {
            if(!arquivo.exists()) arquivo.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String lerArq(String caminho) {
        criarArq(caminho);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caminho));
            String linha;
            while((linha = br.readLine()) != null) sb.append(linha).append("\n");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void escreverArq(String caminho, String texto) {
        criarArq(caminho);
        FileWriter escritor = null;

        try {
            escritor = new FileWriter(new File(caminho), false);
            escritor.write(texto);
            escritor.flush();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(escritor != null)
                    escritor.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void _capturar_pasta() {
		projetosLista.clear();
		projetos.clear();
		listeDir(dirTrabalho.getAbsolutePath(), projetos);

		for(int i = 0; i < projetos.size(); i++) {
			Map<String, Object> _item = new HashMap<>();
			_item.put("caminho", projetos.get(i)); // chave fixa
			projetosLista.add(_item);
		}
		pastas.setAdapter(new PastasAdapter(projetosLista)); // fora do loop
	}

    public class PastasAdapter extends BaseAdapter {
        public List<Map<String, Object>> dados;

        public PastasAdapter(List<Map<String, Object>> arr) {
            dados = arr;
        }

        @Override
        public int getCount() {
            return dados.size();
        }

        @Override
        public Map<String, Object> getItem(int i) {
            return dados.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int posicao, View v, ViewGroup div) {
            LayoutInflater inflador = getLayoutInflater();
            View view = v;
            if(view == null) view = inflador.inflate(R.layout.diretorios, null);

            final ImageView iconeArq = view.findViewById(R.id.iconeArq);
            final TextView texArq = view.findViewById(R.id.texArq);

            String cam = dados.get(posicao).get("caminho").toString();
			texArq.setText(Uri.parse(cam).getLastPathSegment());
            if(cam.endsWith(".asm") || cam.endsWith(".s")) iconeArq.setImageResource(R.drawable.asm);
            else if(cam.endsWith(".png") || cam.endsWith(".jpg")) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4; // diminui a resolucao 4 vezes
                Bitmap imagem = BitmapFactory.decodeFile(cam, opts);
                iconeArq.setImageBitmap(imagem);
            } else if(cam.endsWith(".txt")) iconeArq.setImageResource(R.drawable.txt);
            else if(cam.endsWith(".c")) iconeArq.setImageResource(R.drawable.C);
			else iconeArq.setImageResource(R.drawable.pasta);
            return view;
        }
	}
    
    public void praTerminal(View v) {
        TerminalActivity.comandoPadrao = "";
        if(arquivoAtual != null && !arquivoAtual.equals("") && !nomeArquivo.getText().toString().equals("")) {
            try {
                String caminho;
                if(nomeArquivo.getText().toString().startsWith("/"))  caminho = nomeArquivo.getText().toString();
                else caminho = dirTrabalho.getAbsolutePath() + "/" + nomeArquivo.getText().toString();

                escreverArq(caminho, editor.getText().toString());
                arquivoAtual = caminho;

                Toast.makeText(getApplicationContext(), "arquivo salvo", Toast.LENGTH_SHORT).show();
                _capturar_pasta();
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(), "erro: "+e, Toast.LENGTH_SHORT).show();
            }
            File nomeArquivo = new File(arquivoAtual);
            if(!(new File(getFilesDir().getAbsolutePath()+"/pacotes/bin").isDirectory()) && !nomeArquivo.isDirectory()) {
                TerminalActivity.comandoPadrao = "instalar clang";
            } else {
				TerminalActivity.comandoPadrao += 
					"clang " + nomeArquivo + " -o " + nomeArquivo.getName().replace(".c", "") + "&& " +
					"./" + nomeArquivo.getName().replace(".c", "");
			}
        } else TerminalActivity.comandoPadrao = null;
        Intent t = new Intent(this, TerminalActivity.class);
        startActivity(t);
    }
    
    public void autocomplete(View v) {
        if(AutoCompletar.autocomplete) AutoCompletar.autocomplete = false;
        else AutoCompletar.autocomplete = true;
    }
	
	public void voltar(View v) {
		File pai = dirTrabalho.getParentFile();
		if(pai != null && pai.exists()) {
			dirTrabalho = pai;
			_capturar_pasta();
			Toast.makeText(this, "1 pasta voltada", Toast.LENGTH_SHORT).show();
		} else Toast.makeText(this, "já está na raiz", Toast.LENGTH_SHORT).show();
	}
}
