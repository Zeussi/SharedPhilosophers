package blatt32;

import java.util.Random;

public class DatenAbruf {
	DatenSepp datenSepp = new DatenSepp();
	
	public void start(int counter) {
		// Durchläuft alle Objekte die erzeugt werden sollen
		for(int i = 0; i < counter; i++)
		{
			int random = (new Random().nextInt() % 2);
			random = Math.abs(random);
			// zufällig Erzeuger oder Verbraucher erzeugen
			if(random == 1)
			{
				new Verbraucher(datenSepp).start();
			}
			else
			{
				new Erzeuger(datenSepp).start();
			}		
		}
	}
}
