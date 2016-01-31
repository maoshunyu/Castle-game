package castle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import database.Database;
import funcs.*;
import cells.*;
import map.GameMap;
import util.Echoer;
import util.NameGenerator;

import javax.swing.*;

public class Game
implements Echoer{

	private HashMap<String, FuncSrc> funcs = new HashMap<>();
	private String[] funcsString ;
	private GameMap map;
	private ArrayList<Item> theItems = new ArrayList<>();
	private Player player;
	private Database database;
	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;

	//    构造方法
	public Game(){
		onCreate();
	}

	private void onCreate(){
		map = new GameMap();
		createItems();
		database = new Database();
		funcsString = new String[]{
				"help",
				"go",
				"wild",
				"exit",
				"state",
				"fight",
				"sleep",
				"save",
				"rename"
		};
		funcs.put(funcsString[0], new FuncHelp(this));
		funcs.put(funcsString[1], new FuncGo(this));
		funcs.put(funcsString[2], new FuncWild(this));
		funcs.put(funcsString[3], new FuncBye(this));
		funcs.put(funcsString[4], new FuncState(this));
		funcs.put(funcsString[5], new FuncFight(this));
		funcs.put(funcsString[6], new FuncSleep(this));
		funcs.put(funcsString[7], new FuncSave(this));
		funcs.put(funcsString[8], new FuncRename(this));

//		echo = new StringBuffer();
		textField = new JTextField("在这里输入指令");
		textField.registerKeyboardAction(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HandleMessage(textField.getText());
						textField.setText("");
					}
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
				JComponent.WHEN_FOCUSED
		);
		textArea = new JTextArea();
		frame = new JFrame("城堡游戏   by 千里冰封");
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(
				"." + File.separator + "drawable" + File.separator + "ic_launcher.png"
		));
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(textField, BorderLayout.SOUTH);
		frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
		frame.setResizable(false);
		frame.setVisible(true);
	}

//	private void onResume() {
//		String line;
//		boolean loop = true;
//		Scanner in = new Scanner(System.in);
//		while ( loop ) {
//			echoln("");
//			line = in.nextLine();
//			loop = HandleMessage(line);
//		}
//		in.close();
//	}

	private void onStart() {
		echoln("欢迎来到城堡！");
		echoln("这是一个超复古的CUI游戏。");
		echoln("最新版本和源代码请见https://github.com/ice1000/Castle-game");
		echoln("敬请期待OL版本https://github.com/ProgramLeague/Castle-Online");
//        echoln("不过在经过了冰封的改造后，你会觉得这个很有意思。");
		if(!Database.isFileExists()){
			echoln("您可以稍后使用\"rename [新名字]\"命令来更改自己的名字。");
			player = new Player(NameGenerator.generate(),200,10,5);
			saveData();
		}
		else {
			player = new Player(null,-1,-1,-1);
			database.loadState(player);
			database.loadMap(map,"宾馆");
			echoln("检测到存档。");
		}
		echoln("你好"+player);
		echoln("如果需要帮助，请输入 'help' 。\n");
		echo("现在");
		echoln(map.getCurrentRoomPrompt());
	}

	private boolean HandleMessage(String line){
		String[] words = line.split(" ");
		FuncSrc func = funcs.get(words[0]);
		String value2 = "";

		if( words.length > 1 )
			value2 = words[1];

//			如果找到了该指令
		if( func != null ){
			func.DoFunc(value2);
			if( func.isGameEnded() ){
//					退出指令特殊处理
				saveData();
				echoln("退出游戏，再见！");
//				System.exit(0);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored){}
				frame.dispose();
				return false;
			}
		}
		else
			echoln("对不起，输入指令错误！");
		return true;
	}

	public String[] getFuncs(){
		return funcsString;
	}

	private void createItems() {
		Item wilder;
		theItems.add(wilder = new Item("传送门"));
	}

	@Override
	public void echo(String words){
//		System.out.print(words);
		textArea.append(words);
		int i = textArea.getText().length();
		int MAX_LENGTH = 10000;
		if(i > MAX_LENGTH){
			textArea.setText(textArea.getText().substring(
					i - MAX_LENGTH, i
			));
		}
	}
	@Override
	public void echoln(String words){
		echo(words + "\n");
	}
	/**
	 * 去一个房间
	 */
	public void goRoom(String direction){
		if(!map.goRoom(direction))
			echoln("没有这个出口。");
		echoln(map.getCurrentRoomPrompt());
	}
	/**
	 * 随机传送
	 */
	public void WildRoom(){
		echoln(map.wildRoom());
	}
	/**
	 * 战斗函数
	 */
	public void Fight() {
		map.fightBoss(this);
		echoln(map.getCurrentRoomPrompt());
	}
	public void setPlayer(Player player){
//    	减血赋值给原来的
		this.player = player;
	}
	public Player getPlayer() {
		return player;
	}
	/**
	 * 指定数量的补血
	 */
	public void Treat(int bloodMore) {
		player.blood += bloodMore;
	}
	/**
	 * 补血
	 */
	public boolean Treat() {
		return player.treat();
	}
	/**
	 * 检查是否可以睡觉
	 */
	public boolean TreatRoomCheck() {
		return map.treatRoomCheck();
	}
	/**
	 * 显示玩家数据
	 * @return 玩家数据
	 */
	public String playerToString() {
		return player.stateToString();
//    	return player;
	}

	/**
	 * 返回BOSS是否被打败过
	 * @return BOSS是否被打败过
	 */
	public boolean isBossGetItem() {
		return map.BossGetItem();
	}
	public void saveData(){
		try {
			database.saveMapAndState(map,player);
			echoln("保存成功。");
		} catch (IOException e){
			echoln("保存失败，请检查是否有管理员权限！");
		}
	}

	public static void main(String[] args) {
		Game game = new Game();
		game.onStart();
//		game.onResume();
	}

}
