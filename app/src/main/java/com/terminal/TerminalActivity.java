package com.terminal;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.ScrollView;
import android.text.Editable;
import android.text.TextWatcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import java.net.URL;
import java.io.InputStream;
import java.util.HashMap;
import com.cide.R;

public class TerminalActivity extends Activity { 
    public EditText entrada;
    public TextView saida;
    public static File dirTrabalho;
    public static File dirPs;
	public static final List<String> bins = new ArrayList<>();
    public static final Map<String, String> pacotes = new HashMap<>();
    public Logs logs;
    public static String comandoPadrao;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.terminal);
        
        iniciarPacotes();
        
        if(dirPs == null) dirPs = new File(getFilesDir().getAbsolutePath()+"/pacotes");
        if(!dirPs.isDirectory()) dirPs.mkdirs();
        bins.add(dirPs.getAbsolutePath()+"/include");
        bins.add(dirPs.getAbsolutePath()+"/bin");
        bins.add(dirPs.getAbsolutePath()+"/libs");
        if(dirTrabalho == null) dirTrabalho = new File(getFilesDir().getAbsolutePath()+"/CASA");
        if(!dirTrabalho.isDirectory()) dirTrabalho.mkdirs();
        new File(dirPs.getAbsoluteFile()+"/tmp").mkdir();
        
        entrada = findViewById(R.id.entrada);
        saida = findViewById(R.id.saida);
        
        logs = new Logs(saida);
        
        System.out.println("> [bem vindo ao Terminal Simples]");
        System.out.println("> comandos:");
        System.out.println("> instalar <pacote/caminho>");
        System.out.println("> limp");
        System.out.println(">");
        System.out.println("> pacotes disponiveis");
        System.out.println("> node");
        System.out.println("> asm");
        System.out.println("> clang // só incluí o compilador de C");
        
        if(comandoPadrao != null && !comandoPadrao.equals("")) executar(comandoPadrao);
        entrada.addTextChangedListener(new TextWatcher() {
				public void beforeTextChanged(CharSequence s, int i, int c, int d) {}
				public void onTextChanged(CharSequence s, int i, int a, int c) {}
				public void afterTextChanged(Editable s) {
                    String c = s.toString();
					if(c.endsWith("\n")) {
						System.out.println("> " + c);
                        executar(c);
						s.clear();
					}
				}
		});
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    
    public static void iniciarPacotes() {
        pacotes.put("node", "https://github.com/Shiniga-OP/Terminal-simples-android/releases/download/NodeJS-v22.17.1-aarch64/node.zip");
        pacotes.put("asm", "https://github.com/Shiniga-OP/Terminal-simples-android/releases/download/Assembly-aarch64/asm.zip");
        pacotes.put("clang", "https://github.com/Shiniga-OP/Terminal-simples-android/releases/download/Clang-20.1.8-aarch64/clang.zip");
    }
    
    public void executar(final String comandoStr) {
        new Thread(new Runnable() {
				public void run() {
					if(comandoStr.startsWith("cd ")) {
						executarCd(comandoStr.substring(3).trim());
						return;
					} else if(comandoStr.startsWith("limp")) {
                        saida.setText("");
                        return;
                    } else if(comandoStr.startsWith("instalar ")) {
                        String c = comandoStr.substring(9).trim();
                        if(pacotes.containsKey(c)) instalarWeb(pacotes.get(c));         
                        else {
                            if((new File(c).exists())) instalarPacote(c);
                            else System.out.println("este arquivo não existe");
                        }
                        return;
                    }
					executarProcesso(comandoStr);
				}
		}).start();
    }
    
    public static void executarEs(final String comandoStr, final Activity a) {
        new Thread(new Runnable() {
                public void run() {
					if(comandoStr.startsWith("cd ")) {
						executarCdEs(comandoStr.substring(3).trim(), a);
						return;
					} else if(comandoStr.startsWith("instalar ")) {
                        String c = comandoStr.substring(9).trim();
                        if(pacotes.containsKey(c)) instalarWebEs(pacotes.get(c), a);         
                        else {
                            if((new File(c).exists())) instalarPacoteEs(c, a);
                            else System.out.println("este arquivo não existe");
                        }
                        return;
                    }
					executarProcessoEs(comandoStr);
				}
            }).start();
    }
    
    public static void instalarWebEs(final String url, Activity a) {
        File tmp = new File(a.getFilesDir().getAbsolutePath()+"/tmp");
        if(!tmp.exists()) tmp.mkdirs();
        final File zip = new File(tmp, "pacote.zip");
        try {
            URL u = new URL(url);
            InputStream en = u.openStream();
            FileOutputStream s = new FileOutputStream(zip);
            byte[] buffer = new byte[8192];
            int l;
            while((l = en.read(buffer)) > 0) s.write(buffer, 0, l);
            s.close();
            en.close();
            instalarPacoteEs(zip.getAbsolutePath(), a);
        } catch(Exception e) {}
    }
    
    public void instalarWeb(final String url) {
        runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("baixando pacote...");
                }
            });
        File tmp = new File(dirPs.getAbsolutePath()+"/tmp");
        if(!tmp.exists()) tmp.mkdirs();
        final File zip = new File(tmp, "pacote.zip");
        try {
            URL u = new URL(url);
            InputStream en = u.openStream();
            FileOutputStream s = new FileOutputStream(zip);
            byte[] buffer = new byte[8192];
            int l;
            while((l = en.read(buffer)) > 0) s.write(buffer, 0, l);
            s.close();
            en.close();
            instalarPacote(zip.getAbsolutePath());
        } catch(Exception e) {
            erro("Falha no download: " + e.getMessage());
        }
    }

    public static void executarCdEs(String novoDir, Activity a) {
        if(novoDir.trim().equals("")) {
            dirTrabalho = new File(a.getFilesDir().getAbsolutePath()+"/CASA");
            return;
        }
        File novo;
        if(novoDir.isEmpty()) {
            novo = new File(a.getFilesDir().getAbsolutePath()+"/CASA");
            if(!novo.isDirectory()) novo.mkdir();
        } else if(novoDir.startsWith("/")) novo = new File(novoDir);
        else novo = new File(dirTrabalho, novoDir);

        if(!novo.isDirectory() || !novo.canRead()) {
            return;
        }
        dirTrabalho = novo;
    }
    
    public void executarCd(String novoDir) {
        if(novoDir.trim().equals("")) {
            dirTrabalho = new File(getFilesDir().getAbsolutePath()+"/CASA");
            return;
        }
        File novo;
        if(novoDir.isEmpty()) {
            novo = new File(getFilesDir().getAbsolutePath()+"/CASA");
            if(!novo.isDirectory()) novo.mkdir();
        } else if(novoDir.startsWith("/")) novo = new File(novoDir);
        else novo = new File(dirTrabalho, novoDir);

        if(!novo.isDirectory() || !novo.canRead()) {
            erro("cd: " + novoDir + ": Diretório não encontrado ou sem permissão");
            return;
        }
        dirTrabalho = novo;
        runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("Diretório atual: " + dirTrabalho.getAbsolutePath() + "\n");
                }
            });
    }
    
    public static void executarProcessoEs(String comando) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			Map<String, String> cams = pb.environment();

			String camAtual = cams.get("PATH");
			if(camAtual == null) camAtual = "";
			if(bins != null) {
				for(String bin : bins) {
					if(bin == null) break;
					camAtual = bin + ":" + camAtual;
				}
			}
			cams.put("PATH", camAtual);

			String sysrootBiblis = dirPs.getAbsolutePath() + "/libs";

			String localInclude = dirPs.getAbsolutePath() + "/include";
			String clangInclude = sysrootBiblis + "/usr/clang/20/include";
			String usrInclude = sysrootBiblis + "/usr/include";
			String cAtual = cams.get("C_INCLUDE_PATH");
			if(cAtual == null) cAtual = "";
			cams.put("C_INCLUDE_PATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cAtual);

			String cpathAtual = cams.get("CPATH");
			if(cpathAtual == null) cpathAtual = "";
			cams.put("CPATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cpathAtual);

			String cppAtual = cams.get("CPLUS_INCLUDE_PATH");
			if(cppAtual == null) cppAtual = "";
			cams.put("CPLUS_INCLUDE_PATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cppAtual);

			String bibliDir = sysrootBiblis + "/usr/lib";
			String bibliAtual = cams.get("LIBRARY_PATH");
			if(bibliAtual == null) bibliAtual = "";
			cams.put("LIBRARY_PATH", bibliDir + ":" + sysrootBiblis + ":" + bibliAtual);

			String ldAtual = cams.get("LD_LIBRARY_PATH");
			if(ldAtual == null) ldAtual = "";
			cams.put("LD_LIBRARY_PATH", bibliDir + ":" + sysrootBiblis + ":" + ldAtual);

			cams.put("TMPDIR", dirPs.getAbsolutePath() + "/tmp");
			String openssl = cams.get("OPENSSL_CONF");
			if(openssl == null) openssl = "";
			cams.put("OPENSSL_CONF", dirPs.getAbsolutePath() + "/etc" + ":" + openssl);

			cams.put("SYSROOT", sysrootBiblis);
			cams.put("PACOTES_DIR", dirPs.getAbsolutePath());

			pb.command("/system/bin/sh", "-c", comando);
			pb.directory(dirTrabalho);
			pb.redirectErrorStream(true);

			final Process p = pb.start();
			BufferedReader leitor = new BufferedReader(new InputStreamReader(p.getInputStream()));

			StringBuilder resultado = new StringBuilder();
			String linha;
			while((linha = leitor.readLine()) != null) resultado.append(linha).append("\n");
		} catch(final Exception e) {}
	}
    
    public void executarProcesso(String comando) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			Map<String, String> cams = pb.environment();

			String camAtual = cams.get("PATH");
			if(camAtual == null) camAtual = "";
			if(bins != null) {
				for(String bin : bins) {
					if(bin == null) break;
					camAtual = bin + ":" + camAtual;
				}
			}
			cams.put("PATH", camAtual);

			String sysrootBiblis = dirPs.getAbsolutePath() + "/libs";

			String localInclude = dirPs.getAbsolutePath() + "/include";
			String clangInclude = sysrootBiblis + "/usr/clang/20/include";
			String usrInclude = sysrootBiblis + "/usr/include";
			String cAtual = cams.get("C_INCLUDE_PATH");
			if(cAtual == null) cAtual = "";
			cams.put("C_INCLUDE_PATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cAtual);

			String cpathAtual = cams.get("CPATH");
			if(cpathAtual == null) cpathAtual = "";
			cams.put("CPATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cpathAtual);

			String cppAtual = cams.get("CPLUS_INCLUDE_PATH");
			if(cppAtual == null) cppAtual = "";
			cams.put("CPLUS_INCLUDE_PATH", localInclude + ":" + clangInclude + ":" + usrInclude + ":" + cppAtual);

			String bibliDir = sysrootBiblis + "/usr/lib";
			String bibliAtual = cams.get("LIBRARY_PATH");
			if(bibliAtual == null) bibliAtual = "";
			cams.put("LIBRARY_PATH", bibliDir + ":" + sysrootBiblis + ":" + bibliAtual);

			String ldAtual = cams.get("LD_LIBRARY_PATH");
			if(ldAtual == null) ldAtual = "";
			cams.put("LD_LIBRARY_PATH", bibliDir + ":" + sysrootBiblis + ":" + ldAtual);

			cams.put("TMPDIR", dirPs.getAbsolutePath() + "/tmp");
			String openssl = cams.get("OPENSSL_CONF");
			if(openssl == null) openssl = "";
			cams.put("OPENSSL_CONF", dirPs.getAbsolutePath() + "/etc" + ":" + openssl);

			cams.put("SYSROOT", sysrootBiblis);
			cams.put("PACOTES_DIR", dirPs.getAbsolutePath());
			
			pb.command("/system/bin/sh", "-c", comando);
			pb.directory(dirTrabalho);
			pb.redirectErrorStream(true);

			final Process p = pb.start();
			BufferedReader leitor = new BufferedReader(new InputStreamReader(p.getInputStream()));

			StringBuilder resultado = new StringBuilder();
			String linha;
			while((linha = leitor.readLine()) != null) resultado.append(linha).append("\n");

			final String s = resultado.toString();
			final int codigoSaida = p.waitFor();

			runOnUiThread(new Runnable() {
					public void run() {
						System.out.println(s);
						System.out.println("saida: " + codigoSaida + "\n");
					}
				});
		} catch(final Exception e) {
			erro(e.getMessage());
		}
	}

    public void erro(final String msg) {
        runOnUiThread(new Runnable() {
				public void run() {
					System.out.println("erro: " + (msg != null ? msg : "Erro desconhecido") + "\n");
				}
		});
    }
    
    public static void instalarPacoteEs(String cam, Activity a) {
        a.runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("instalando pacote...");
                }
            });
        final File zipArq = new File(cam);
        if(!zipArq.exists()) return;
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArq));
            ZipEntry entradas;
            File destDir = new File(dirPs.getAbsolutePath());
            if(!destDir.exists() && !destDir.mkdirs()) {
                return;
            }
            byte[] buffer = new byte[8192];
            while((entradas = zis.getNextEntry()) != null) {
                if(entradas.isDirectory()) continue;
                File saidaArq = new File(destDir, entradas.getName());
                File parente = saidaArq.getParentFile();
                if(!parente.exists() && !parente.mkdirs()) {
                    zis.closeEntry();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(saidaArq);
                int tam;
                while((tam = zis.read(buffer)) > 0) fos.write(buffer, 0, tam);
                fos.getFD().sync();
                fos.close();

                if(!saidaArq.setExecutable(true, false)) {
                    Process chmod = Runtime.getRuntime().exec(new String[]{ "chmod", "+x", saidaArq.getAbsolutePath() });
                    chmod.waitFor();
                }
                zis.closeEntry();
            }
            zis.close();
            executarEs("find " + dirPs.getAbsolutePath() + "/bin -type f -exec chmod +x {} \\;", a);
            a.runOnUiThread(new Runnable() {
                    public void run() {
                        System.out.println("[PACOTE INSTALADO]");
                    }
                });
            if(cam.startsWith(a.getFilesDir().getAbsolutePath()+"/tmp/")) zipArq.delete();
        } catch(Exception e) {}
    }
    
    public void instalarPacote(String cam) {
        runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("instalando pacote...");
                }
            });
        final File zipArq = new File(cam);
        if(!zipArq.exists()) return;
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArq));
            ZipEntry entradas;
            File destDir = new File(dirPs.getAbsolutePath());
            if(!destDir.exists() && !destDir.mkdirs()) {
                erro("Falha ao criar diretório pacotes");
                return;
            }
            byte[] buffer = new byte[8192];
            while((entradas = zis.getNextEntry()) != null) {
                if(entradas.isDirectory()) continue;
                File saidaArq = new File(destDir, entradas.getName());
                File parente = saidaArq.getParentFile();
                if(!parente.exists() && !parente.mkdirs()) {
                    erro("Falha ao criar diretório " + parente.getAbsolutePath());
                    zis.closeEntry();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(saidaArq);
                int tam;
                while((tam = zis.read(buffer)) > 0) fos.write(buffer, 0, tam);
                fos.getFD().sync();
                fos.close();

                if(!saidaArq.setExecutable(true, false)) {
                    Process chmod = Runtime.getRuntime().exec(new String[]{ "chmod", "+x", saidaArq.getAbsolutePath() });
                    chmod.waitFor();
                    if(!saidaArq.canExecute()) erro("Não foi possível definir permissão de execução: " + saidaArq.getName());
                }
                zis.closeEntry();
            }
            zis.close();
            executar("find " + dirPs.getAbsolutePath() + "/bin -type f -exec chmod +x {} \\;");
            runOnUiThread(new Runnable() {
                    public void run() {
                        System.out.println("[PACOTE INSTALADO]");
                    }
                });
            if(cam.startsWith(getFilesDir().getAbsolutePath()+"/tmp/")) zipArq.delete();
        } catch(Exception e) {
            erro("Erro ao instalar binários: " + e.getMessage());
        }
    }
}
