package blatt32;

public class ErzeugerVerbraucher {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int counter;
		if(args.length == 0)
			counter = 20;
		else
			counter = new Integer(args[0]);
		
		DatenAbruf abruf = new DatenAbruf();
		abruf.start(counter);
	}
}
