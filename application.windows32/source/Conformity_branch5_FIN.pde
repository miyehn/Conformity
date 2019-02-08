/*
import gifAnimation.*;
GifMaker exp;

void setup(){
  size(720,480);
  setup_();
  exp = new GifMaker(this,"exp-fin.gif"); //name animation
  exp.setRepeat(0); //0 = forever
  exp.setDelay(40); //frameRate = 1000/setDelay
}

void draw()
{
  draw_();
  if(mousePressed && frameCount % 1 == 0)//save every frame or every how many frames)
  {
    exp.addFrame(); 
  }
}

void mousePressed()
{
  println("record started");
}

void mouseReleased()
{
  exp.finish();
  println("record finished");
}
*/

boolean[] keys;
PShape keyGroup;
Grid grid;
Crowd crowd;
Individual user;

void setup()
{
  size(720,480);
  frameRate(25);
  colorMode(HSB,360,100,100,255);
  //keys
  keys = new boolean[4];
  keyGroup = createShape(GROUP);
  initializeKeyGroup();
  //other initializations
  grid = new Grid(120);
  crowd = new Crowd(800);
  user = new Individual();
  //background display info (food, which relies on global def)
  noStroke();
}

void draw()
{
  background(0);
  // displayKeyGroup();
  crowd.run();
  user.run();
  println(user.numFans);
}

void keyPressed()
{
  if(key==CODED)
  {
    if(keyCode==UP) keys[0]=true;
    else if(keyCode==DOWN) keys[1]=true;
    else if(keyCode==LEFT) keys[2]=true;
    else if(keyCode==RIGHT) keys[3]=true;
  }
}

void keyReleased()
{
  if(key==CODED)
  {
    if(keyCode==UP) keys[0]=false;
    else if(keyCode==DOWN) keys[1]=false;
    else if(keyCode==LEFT) keys[2]=false;
    else if(keyCode==RIGHT) keys[3]=false;
  }
}

//----helper methods for key display----

void initializeKeyGroup()
{
  PShape up = createShape();
  up.beginShape();
  //up.noStroke();
  up.vertex(35,0);//upper left vertex
  up.vertex(65,0);
  up.vertex(65,20);
  up.vertex(35,20);
  //cut out triangle
  up.beginContour();
  up.vertex(35+15,5);
  up.vertex(35+10,13);
  up.vertex(35+20,13);
  up.endContour();
  up.endShape(CLOSE);
  //add to keyGroup
  keyGroup.addChild(up);
  
  PShape down = createShape();
  down.beginShape();
  //down.noStroke();
  down.vertex(35,25);//upper left
  down.vertex(65,25);
  down.vertex(65,45);
  down.vertex(35,45);
  //cut out triangle
  down.beginContour();
  down.vertex(35+10,25+7);
  down.vertex(35+15,25+15);
  down.vertex(35+20,25+7);
  down.endContour();
  down.endShape(CLOSE);
  //add to keyGroup
  keyGroup.addChild(down);
  
  PShape left = createShape();
  left.beginShape();
  //left.noStroke();
  left.vertex(0,25);//upper left
  left.vertex(30,25);
  left.vertex(30,45);
  left.vertex(0,45);
  //cut out triangle
  left.beginContour();
  left.vertex(9,25+10);
  left.vertex(19,25+14);
  left.vertex(19,25+6);
  left.endContour();
  left.endShape(CLOSE);
  //add to keyGroup
  keyGroup.addChild(left);
  
  PShape right = createShape();
  right.beginShape();
  //right.noStroke();
  right.vertex(70,25);//upper left
  right.vertex(100,25);
  right.vertex(100,45);
  right.vertex(70,45);
  //cut out triangle
  right.beginContour();
  right.vertex(70+11,25+6);
  right.vertex(70+11,25+14);
  right.vertex(70+21,25+10);
  right.endContour();
  right.endShape(CLOSE);
  //add to keyGroup
  keyGroup.addChild(right);
}


void displayKeyGroup()
{
  for(int i=0; i<keyGroup.getChildCount(); i++)
  {
    if(keys[i])
    {
      keyGroup.getChild(i).setStroke(0);
      keyGroup.getChild(i).setFill(color(0,0,100,200));
    }
    else
    {
      keyGroup.getChild(i).setStroke(200);
      keyGroup.getChild(i).setFill(0);
    }
  }
  keyGroup.translate(width-15-100,height-15-45);
  shape(keyGroup);
  keyGroup.resetMatrix();
}