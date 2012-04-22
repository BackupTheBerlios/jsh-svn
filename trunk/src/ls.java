import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;

public class ls extends JavaCommand
{
  public int run(BufferedReader inputStream, PrintWriter outputStream)
  {
Dprintf.dprintf("run ls !!!!!!!!!!!!!!!!!!");
//Dprintf.dprintf("arguments[0]=%s",arguments[0]);

    File[] files = new File(".").listFiles();
    if (files != null)
    {
      for (File file : files)
      {
        outputStream.println(file);
outputStream.flush();
      }
    }

    return -1;
  }
}
