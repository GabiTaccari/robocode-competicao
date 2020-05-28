package competicao;
//importação das bibliotecas
import robocode.*;
import java.awt.*;
import robocode.Robot;
import java.awt.Color;
import java.util.*;
import java.awt.geom.Point2D;
import java.lang.Math.*;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Coxinha
 */

public class Coxinha2 extends AdvancedRobot
{
	//Variáveis utilizadas para cálculos de tiro e movimento
 	double angulo_bearing, dist, ener, angulo_heading, veloc, x, y; //Ângulo robÔ adversário, distância, energia, ângulo em relação à tela, velocidade, coordenada x e coordenada y
	private String nome; //Armazena o nome
	private	int moveDirection = 1; //Váriavel privada utilizada para a locomoção

	//criacao das variáveis para cor
	Random generator = new Random();
	int cor1=0;
	int	cor2=0;
	int cor3=0;

 	public void run() { //Executado quando somente uma vez quando o round inicia

  	//Altera as cores do robô
	setBodyColor(Color.magenta);     //Corpo
  	setGunColor(Color.black);	 	 //Arma
  	setRadarColor(Color.blue);       //Radar
  	setBulletColor(Color.cyan);      //Balas
  	setScanColor(Color.cyan);		 //Scan
	
	//Define as partes do robô como separadas entre sí
	setAdjustRadarForGunTurn(true);
	setAdjustGunForRobotTurn(true);
	resetar();				 //Chama o evento reset
	setTurnRadarRight(360);  //Rotaciona o radar em 360º graus para à direita
	
	//Loop enquanto o robô estiver vivo, ou seja, true
	while (true) {
		setTurnRadarRight(360); //Gira o radar 360º para a direitra
		andar();                //Chamada para o evento andar
		atirar();			    //Chamada para o evento atirar
		execute();		 	    //executa
	}
 }
	//Evento responsável pelo auxílio da verificaçao se é necessario realizar alteração nas coordenadas ou demais atributos, atuando juntamente como o ScannedRobotEvent
	public boolean none()
	{
		if (nome == null || nome == "")
			return true;
		else
			return false;
	}

	//Evento executado quando encontra algum robô no scan
	public void onScannedRobot(ScannedRobotEvent e) 
	{
		//Realiza uma condição para verificar se a distância é menor que 70 e se o robô é o mesmo de antes executando alterações se necessário
		if (none() ||  e.getDistance() < dist - 70 || e.getName().equals(nome)) 
		{
			alteracao(e, this); //Caso o "if" for verdadeiro, realiza à chamada do método alteração passando como parâmetro 'e' (contendo os dados do robô inimigo)
		}
		
		cor1=generator.nextInt( 500 );
		cor2=generator.nextInt( 255 );
		cor3=generator.nextInt( 255 );
		setColors(new Color(cor1,cor2,cor3),new Color(cor1,cor2,cor3),new Color(cor1,cor2,cor3),Color.darkGray,new Color(cor1,cor2,cor3));
	}
	
	//Evento responsável por realizar alterações nas informações de um robô adversário caso solicitado no evento scanned robot
	public void alteracao (ScannedRobotEvent e, Robot robot) //É passado o parâmetro que possibilita acesso aos dados do robõ inimigo quando scaneado e do próprio robô 
	{
		angulo_bearing = e.getBearing(); //Retorna o ângulo do robô adversário em relaçao ao robo
		dist = e.getDistance();			 //Retorna a distância do robô adversário
		ener = e.getEnergy(); 			 //Retorna a energia do robô adversário
		angulo_heading = e.getHeading(); //Retorna o ângulo do robô adversário em relação à tela
		veloc = e.getVelocity(); 		 //Retorna à velocidade do robô adversário
		nome = e.getName(); 			 //Armazena o nome do robô adversário
	
	//O código a seguir calcula o rolamento absoluto entre o robô e o inimigo
		double redic= (robot.getHeading() + e.getBearing()); //Soma o ângulo do robô em relação a tela com o ângulo do robô adversário em relação ao robô
		if (redic < 0){
			redic +=360; //Aumenta em 360º à medida calculada anteriormente se a mesma for menor que zero
		}
		//Definição da variável x:
		x = robot.getX() + Math.sin(Math.toRadians(redic)) * e.getDistance();
			 //Soma a coordenada x do robô com o valor do seno em radianos calculado anteriormente multiplicado pela distância do robô inimigo
		 //Resumidamente, o código anterior calcula o comprimento do lado oposto de um triângulo e, em seguida, o desloca pelo valor X do nosso robô.~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		 
		//Definição da variável y
		y = robot.getY() + Math.cos(Math.toRadians(redic)) * e.getDistance(); //Realiza a mesma açao que na linha anterior, porem com o cosceno ao inves do seno.
		//Resumidamente, calcula o comprimento oposto de um triângulo e descola pelo valor Y do robô~~~~~~~~~~~~~~~~
	}
	
	//Evento executado quando o robô morre
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(nome)) { //Estabelece condição comparando o nome do robô adversário com o nome armazenado no início do código
			resetar(); //Realiza a chamada do método resetar se à condição for verdadeira
		}
	}   

	//Evento responsável por calcular a precisão do tiro e atirar
	void atirar() {
		if (none()) //Realiza uma condição para retornar o que esta armazenada em none
			return;

		double tiroMax = Math.min(300 / dist, 3);  //A função math.min será responsável por calcular à força do tiro baseado na distância
		double velocBala = 20 - tiroMax * 2.2;     //Calcula à velocidade da bala se atirada na potência calculada na linha anterior~~~~~~~~~~~~~~~~~~~~~~~~~~
		long tempo = (long)(dist / velocBala); //Calcula o tempo necessário para atirar baseado na potência e na velocidade calculadas anteriormente
		
		double futuroX = getFutureX(tempo); 
		double futuroY = getFutureY(tempo);
		double grauTiro = angAbsoluto(getX(), getY(), futuroX, futuroY); //Retorna o valor absoluto dos ângulos
		setTurnGunRight(normalizarAng(grauTiro - getGunHeading())); //Normaliza as voltas da arma para a direita através da função normalizarAng
		
	
		//condição para pegar o aquecimento da arma e verificar se ela está apontada para o alvo
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) { //Abs retorna o valor absoluto do mesmo tipo do parâmetro 
			setFire(tiroMax);//Atira com potência maxima
		}
	}
	//Angulação absoluta:
	double angAbsoluto (double x1, double y1, double x2, double y2) {
		//Calcula as coordenadas iniciais
		double xInic = x2-x1;
		double yInic = y2-y1;
		double coord = Point2D.distance(x1, y1, x2, y2); //Gera um plano cartesiano para facilitar contas de distâncias e outras funções que foram baseadas em algoritmos pré planejados e modificadas
		double senoArco = Math.toDegrees(Math.asin(xInic / coord)); //Gera à medida do arco do scan em graus baseado nas coordenadas estabelecidas em x inicial
		double ang_bearing = 0; //Zera o ângulo do robô adversário em relação ao robô
		//Condições para definir à localização do do robô inimigo em relação ao robô
		if (xInic > 0 && yInic > 0) { 
			ang_bearing = senoArco;
		} else if (xInic < 0 && yInic > 0) {
			ang_bearing = 360 + senoArco; 
		} else if (xInic > 0 && yInic < 0) {
			ang_bearing = 180 - senoArco;
		} else if (xInic < 0 && yInic < 0) { 
			ang_bearing = 180 - senoArco;
		}
		return ang_bearing; //Retorna o ângulo do robô adversário
	}

	//Normaliza um rolamento entre -180 e +180
	double normalizarAng(double ang) {
		while (ang >  180)
		{
			ang -= 360; //ang = ang-360
		}
		while (ang < -180)
		{
			ang += 360;
		}
		return ang;
	}

	//Evento de colisão com à parede
	public void onHitWall(HitWallEvent e) {
		setAhead(150); //Avança 150 pixels para frente
	 }

	//Evento andar
	public void andar() {
		setTurnRight(normalizarAng(angulo_bearing + 90 - (10 * moveDirection))); //calcula uma distância para o robô mover-se para à direita
		//Condiçao que estabelece à ação a ser realizada em determinados intervalos de tempo, não necessariamente toda vez que o método rodar
		if (getTime() % 15 == 0) 
		{
			moveDirection *= -1; //moveDirection = moveDirection * -1 --- altera cada vez que rodar entre positivo e negativo, fazendo o robô não andar em uma linha constante e reta
			setAhead(100 * moveDirection); //Anda pra frente
		}
	}
	//Método resetar: "limpa" todos os dados armazenados sobre o robô adversário
	public void resetar(){
		angulo_bearing = 0.0;
		dist =0.0;
		ener= 0.0;
		angulo_heading =0.0;
		veloc = 0.0;
		nome = null;
		x = 0;
		y = 0;
	}

	//Métodos de acesso das coordenadas:  levam um parâmetro long e retornam double
	
	//Calcula as coordenadas
	public double getFutureX(long when){
		//Implementação do método::
		return x + Math.sin(Math.toRadians(angulo_heading)) * veloc * when; //Ira somar a coordenada x calculada com o valor do seno de acordo com angulo em radianos em que o robo esta virado, multiplicado pela velocidade e o parâmetro passado pelo método
		}
	public double getFutureY(long when){
		//Implementação do método:
		return y + Math.cos(Math.toRadians(angulo_heading)) * veloc * when; //Realiza a mesma açao da ultima linha do método anterior, porem com o cosceno ao inves do seno
	}
	//O getFutureX e getFutureY, usam Rate*Time, ou seja, (getVelocity*quando) ao invés da distância do inimigo (usado pelo getX e getY).~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}