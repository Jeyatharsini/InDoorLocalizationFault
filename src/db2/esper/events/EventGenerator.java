package db2.esper.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.espertech.esper.client.EPRuntime;

import db2.esper.event.models.SensorEvent;

/* Rilevamenti:
 * A: senza fault
 * B: senza fault
 * C: senza fault
 * D: senza device di localizzazione
 * E: con un pir non attivo
 * 
 */

public class EventGenerator extends Thread {
	
	protected int periodMS=1000;
	protected boolean verbose=true;
	
	protected EPRuntime cepRT;
	protected String filePath;
	protected int[] defaultSensorState={};
	protected Scanner scanFile; 
	protected String lastLine="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
	
	public EventGenerator(EPRuntime cepRT, String[] args) {
		super();
		this.cepRT = cepRT;
		this.filePath = args[0];
//		if (args.length>1)
//			this.periodMS = new Integer(args[1]);
		if (args.length>2)
			this.verbose = (new Integer(args[2]))>0;
		
		
		try {
			setParser();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setParser() throws FileNotFoundException{
		scanFile= new Scanner(new File(filePath));
	}
	
	@Override
	public void run(){
		int second=0;
		while(scanFile.hasNextLine()){
			String curLine=scanFile.nextLine();
			
			if(!curLine.contentEquals(lastLine)){
				genEvent(curLine,second);
				lastLine=curLine;
			}
			second++;
			try {
				Thread.sleep(periodMS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	
	/*
	 * Generiamo le classi Event -> tuple
	 * sistemiamo il parser per popolare le nostre tuple
	 * buttiamo tutto sugli stream
	 * 		- stream uno per sensore o uno per categoria di sensore?
	 * 		- Tutti i sensori usano lo stesso evento SensorEvent
	 */
	private void genEvent(String inLine, int second){
		Scanner scan= new Scanner(inLine);
		scan.useDelimiter(" ");
		Scanner lastScan= new Scanner(lastLine);
		scan.useDelimiter(" ");
		byte sensId=0;
		while(scan.hasNext()){
			sensId++;
			String newTok=scan.next();
			String oldTok=lastScan.next();
			if(!newTok.contentEquals(oldTok)){
				if (sensId>20){
					GroundTruth ev = new GroundTruth(sensId,new Byte(newTok),second);
					if (verbose)
						System.out.println(ev.toString());
					cepRT.sendEvent(ev);
				} else {
					SensorEvent ev = new SensorEvent(sensId,new Byte(newTok),second);
					if (verbose)
						System.out.println(ev.toString());
					cepRT.sendEvent(ev);
				}
			}
		}
		scan.close();
		lastScan.close();
	};
	
}
