import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Conformity_branch5_FIN extends PApplet {

/*
import gifAnimation.*;
GifMaker exp;

void setup(){
  
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

public void setup()
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

public void draw()
{
  background(0);
  // displayKeyGroup();
  crowd.run();
  user.run();
  println(user.numFans);
}

public void keyPressed()
{
  if(key==CODED)
  {
    if(keyCode==UP) keys[0]=true;
    else if(keyCode==DOWN) keys[1]=true;
    else if(keyCode==LEFT) keys[2]=true;
    else if(keyCode==RIGHT) keys[3]=true;
  }
}

public void keyReleased()
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

public void initializeKeyGroup()
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


public void displayKeyGroup()
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
class Boid
{
  //static info
  PVector coord;
  int gridR;
  int gridC;
  ArrayList<Boid> neighbors;
  //motion info
  float speed0;//original speed
  PVector velocity;
  float force0;
  PVector acceleration;
  //display info
  PShape shape;
  int rgb;
  float opacity;
  
  //others
  boolean isFan = false;
  Individual idol = null;
  float conformity = 1; //because it's for npcs
  float repelZone;//repel zone for sepVector
  
  //constructor
  Boid(float x, float y)
  {
    this.coord = new PVector(x, y);
    this.speed0 = random(3.95f,4.05f);
    float tmp = random(TWO_PI);
    this.velocity = new PVector(cos(tmp),sin(tmp));
    this.force0 = random(0.029f,0.032f);
    this.acceleration = new PVector(0,0);
    updateNeighbors();
    this.shape = makeShape();
  }

  public void update()//checked
  {
    grid.removeElem(this);
    updateNeighbors();
    if(isFan) conformity = idol.conformity;
    else conformity = 1;
    PVector separation = sepVector().mult(3);
    PVector cohesion = cohVector().mult(0.25f);
    PVector alignment = aliVector().mult(0.4f);
    PVector cohAndAli = PVector.add(cohesion, alignment);
    cohAndAli.limit(4);
    //add three vectors with weight and limit them to force0. Then apply force.
    acceleration.add(separation);
    acceleration.add(cohAndAli);
    //acceleration.limit(force0);
    velocity.add(acceleration);
    coord.add(velocity);
    coord.add(grid.toMove);
    acceleration.mult(0);
    borders(coord);
    grid.encodeElem(this);
  }
  
  public void display()//checked
  {
    shape.translate(coord.x, coord.y);
    shape.rotate(velocity.heading());
    if (isFan) shape.setStroke(idol.strokeColor);
    else shape.setStroke(360);
    shape(shape);
    shape.resetMatrix();
  }
  
  //----
  
  public PShape makeShape()
  {
    PShape res = createShape();
    res.beginShape();
    res.fill(color(0),0);
    res.stroke(360);//idk why the grey max is 360
    res.vertex(0,-3);
    res.vertex(0,3);
    res.vertex(9,0);
    res.endShape(CLOSE);
    return res;
  }

  public void updateNeighbors()//checked
  {
    //clear old neighbors list
    neighbors = new ArrayList<Boid>();
    //first get all elems from adjacent grids and store into sqList
    ArrayList<Boid> sqList= new ArrayList<Boid>();
    for(int i=gridR-1; i<=gridR+1; i++)
    {
      //if(i>=0 && i<grid.r)
      for(int j=gridC-1; j<=gridC+1; j++)
      {
        //if(j>=0 && j<grid.c)
        int i2=i; int j2=j;
        if(i<0) i2 += grid.r; else if (i>=grid.r) i2=0;
        if(j<0) j2 += grid.c; else if (j>=grid.c) j2=0;
        for(int k=0; k<grid.object[i2][j2].size(); k++)
        {
          sqList.add(grid.object[i2][j2].get(k));
        }
      }
    }
    //then put elems into neighborList if within distance
    for(int i=0; i<sqList.size(); i++)
    {
      PVector dif = PVector.sub(this.coord, sqList.get(i).coord);
      if (dif.mag()<grid.gridSize) neighbors.add(sqList.get(i));
    }
  }

  
  //these three adjustments ultimately change velocity
  //kinda works
  public PVector aliVector()
  {
    PVector avg;
    if(isFan) avg = idol.velocity;
    else avg = avgNeighborsVelocity();
    //turn this boid's velocity toward the average
    PVector dif = PVector.sub(avg,velocity);
    if(!isFan)dif.limit(force0);
    return dif;
  }
  
  public PVector cohVector()
  {
    PVector avg;
    if(isFan) avg = idol.coord;
    else avg = avgNeighborsPosition();
    //turn it toward the center of its neighbors
    return moveTo(avg).limit(force0 * 8);
  }
  
  public PVector sepVector()//change into taking a variable
  {
    PVector steer = new PVector(0,0);
    int count = 0;
    repelZone = grid.gridSize* (0.2f+(1-conformity)*0.8f);//aware zone linearly related to conformity
    for(int i=0; i<neighbors.size(); i++)
    {
      //check distance with each neighbor
      PVector dif = PVector.sub(coord,neighbors.get(i).coord);
      if(dif.mag()>0 && dif.mag()<neighbors.get(i).repelZone)//enters neighbor's repel zone
      {
        //add all "to move away" vectors to steer
        //PVector tmp = dif.copy(); tmp.normalize(); tmp.setMag(sqrt(tmp.mag()));
        //dif.setMag(tmp.mag()*dif.mag());
        dif.mult(1.5f);//arbitrary var
        steer.add(dif);
        count++;
      }
    }
    if (count > 0) 
    steer.div((float)count);
    //apply steer to a, then v, then coord
    if (steer.mag()>0)
    {
      steer.setMag(speed0);
      steer.sub(velocity);
      steer.limit(force0*1.5f);
    }
    return steer;
  }
  
  //----helpers----
  
  public void moveToward(PVector target)//checked
  {
    PVector difCoord = PVector.sub(target,coord);
    difCoord.setMag(speed0);
    PVector difVel = PVector.sub(difCoord,velocity);
    difVel.limit(force0);
    //apply to a then v then coord
    acceleration=difVel;
    velocity.add(acceleration);
    coord.add(velocity);
  }
  
  public PVector moveTo(PVector target)
  {
    PVector difCoord = PVector.sub(target,coord);
    difCoord.setMag(speed0);
    PVector difVel = PVector.sub(difCoord,velocity);
    if(!isFan)difVel.limit(force0);
    return difVel;
  }
  
  public PVector avgNeighborsVelocity()//checked
  {
    PVector sum = new PVector(0,0);
    int count = 0;
    for(int i=0; i<neighbors.size(); i++)
    {
      if(this != neighbors.get(i))
      {
        sum.add(neighbors.get(i).velocity);
        count++;
      }
    }
    if (count>0)
      sum.div(count);
    return sum;
  }
  
  public PVector avgNeighborsPosition()//checked
  {
    PVector sum = new PVector(0,0);
    int count = 0;
    for(int i=0; i<neighbors.size(); i++)
    {
      if(this != neighbors.get(i))
      {
        sum.add(neighbors.get(i).coord);
        count++;
      }
    }
    if (count>0)
      sum.div(count);
    return sum;
  }
  
  public void borders(PVector vec) {
    while (vec.x < -grid.bleed) vec.x += grid.canvasWidth;
    while (vec.y < -grid.bleed) vec.y += grid.canvasHeight;
    while (vec.x > grid.canvasWidth) vec.x -= grid.canvasWidth;
    while (vec.y > grid.canvasHeight) vec.y -= grid.canvasHeight;
  }
  
  public boolean out(PVector vec)
  {
    if(vec.x < -grid.bleed || vec.x > grid.canvasWidth ||
       vec.y < -grid.bleed || vec.y > grid.canvasHeight)
      return true;
    return false;
  }
}

class Crowd
{
  int crowdSize;
  ArrayList<Boid> list;
  
  Crowd(int crowdSize)
  {
    this.crowdSize = crowdSize;
    list = new ArrayList<Boid>(crowdSize);
    for(int i=0; i<crowdSize; i++)
    {
      list.add(new Boid(width/2,height/2));
    }
  }
  
  public void run()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).update();
      list.get(i).display();
    }
  }
  
  public void addNew(float x, float y)
  {
    list.add(new Boid(x,y));
  }
  
  //----
  
  public void update()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).update();
    }
  }
  
  public void display()
  {
    for(int i=0; i<list.size(); i++)
    {
      list.get(i).display();
    }
  }
  
}

class Food
{
  float r;
  PVector coord;
  int fillColor;
  
  Food(float x, float y)
  {
    this.r = random(8,20);
    this.coord = new PVector(x,y);
    this.fillColor = color(random(360),random(60,70),random(75,90),180);
  }
  
}
class Grid
{
  int gridSize;
  int bleed;
  int canvasWidth;
  int canvasHeight;
  int r;
  int c;
  ArrayList<Boid>[][] object;//a 2d array of arraylists
  
  PVector toMove;
  
  Grid (int gridSize)
  {
    this.gridSize = gridSize;
    this.bleed = gridSize*2;
    this.canvasWidth = width+bleed*2;
    this.canvasHeight = height+bleed*2;
    this.r = canvasHeight/gridSize+1;
    this.c = canvasWidth/gridSize+1;
    object = new ArrayList[r][c];
    for(int i=0; i<r; i++)
    {
      for(int j=0; j<c; j++)
      {
        object[i][j] = new ArrayList<Boid>();
      }
    }
    this.toMove = new PVector(0,0);
  }
  
  public void removeElem(Boid B)
  {
    object[B.gridR][B.gridC].remove(B);
  }
  
  public void encodeElem(Boid B)
  {
    int elemR = (((int)B.coord.y + bleed) / gridSize) % r;
    int elemC = (((int)B.coord.x + bleed) / gridSize) % c;
    B.gridR = elemR; B.gridC = elemC;
    object[elemR][elemC].add(B);
  }
  
  public ArrayList<Boid> returnElemList(int r, int c)
  {
    return object[r][c];
  }
  
  //----
  
  public void calcToCenter(PVector vec)
  {
    this.toMove = vec.mult(0.05f);//arbitrary variable / camera moving speed
  }
  
}
class Individual extends Boid
{
  PVector refCoord;//record the coord of camera
  PVector[] foodCoordInit;
  Food[] food;
  int numFood;
  
  float[] confs;
  int lastInd;
  float dgrControl;
  
  //color info
  int fillColor;
  int strokeColor;
  float alpha;
  
  //fans info
  int numFans;
  ArrayList<Boid> fans;
  
  Individual()
  {
    super(width/2,height/2);
    this.refCoord = new PVector(0,0);
    this.confs = new float[10];
    this.lastInd=0;
    //make background
    this.numFood = 20;
    this.foodCoordInit = new PVector[numFood];
    this.food = new Food[numFood];
    for(int i=0; i<foodCoordInit.length; i++)
    {
      float x = random(-grid.bleed, grid.canvasWidth-grid.bleed);
      float y = random(-grid.bleed, grid.canvasHeight-grid.bleed);
      foodCoordInit[i] = new PVector(x,y);
      food[i] = new Food(foodCoordInit[i].x,foodCoordInit[i].y);
    }
    this.fillColor=color(255);
    this.alpha=0;
    this.strokeColor=color(255);
    this.fans = new ArrayList();
  }
  
  public void run()
  {
    update();
    display();
  }
  
  public void update()
  {
    grid.removeElem(this);
    updateFans();
    updateNeighbors();
    //from flow
    PVector separation = sepVector().mult(3);
    PVector cohesion = cohVector().mult(0.25f);
    PVector alignment = aliVector().mult(0.4f);
    PVector flow = PVector.add(separation,cohesion).add(alignment);//flow vector
    //from control
    PVector control = ctrVector();//control vector
    
    updateConformity();
    updateDgrControl();
    acceleration.add(flow.mult(1-dgrControl));//weigh flow according to conformity
    acceleration.add(control.mult(dgrControl));//weigh control according to conformity
    //accumulate a into x
    velocity.add(acceleration);
    velocity.limit(4);//max velocity
    coord.add(velocity);
    acceleration.mult(0);
    
    //background stuff
    grid.calcToCenter(PVector.sub(new PVector(width/2,height/2), coord));//store camera move
    coord.add(grid.toMove);
    refCoord.add(grid.toMove);
    borders(coord);
    grid.encodeElem(this);
    //update food stuff
    bite();
    for(int i=0; i<numFood; i++)
    {
      food[i].coord = PVector.add(foodCoordInit[i],refCoord);
      borders(food[i].coord);
    }
  }
  
  public PVector ctrVector()
  {
    PVector steer = new PVector(0,0);
    float theta = velocity.heading();
    if(keys[0])
    {
      PVector toAdd = new PVector(cos(theta),sin(theta));
      toAdd.setMag(force0 * 2);
      steer.add(toAdd);
    }
    
    if(keys[1])
    {
      PVector toAdd = new PVector(cos(theta+PI),sin(theta+PI));
      if(velocity.mag()>=force0*3)
      {
        toAdd.setMag(force0 * 3);
        steer.add(toAdd);
      }
      else
        steer.add(new PVector(0,0));
    }
        
    if(keys[2])
    {
      PVector toAdd = new PVector(cos(theta-HALF_PI),sin(theta-HALF_PI));
      toAdd.setMag(force0 * 2 * velocity.mag());
      steer.add(toAdd);
    }
        
    if(keys[3])
    {
      PVector toAdd = new PVector(cos(theta+HALF_PI),sin(theta+HALF_PI));
      toAdd.setMag(force0 * 2 * velocity.mag());
      steer.add(toAdd);
    }
    return steer.mult(1.2f);//weight
  }
  
  public boolean controlling()
  {
    if(keyPressed && key==CODED)
    {
      if(keyCode==UP || keyCode==DOWN ||
           keyCode==LEFT|| keyCode==RIGHT)
        return true;
    }
    return false;
  }

  
  public void updateConformity()
  {
    if(neighbors.size()==0)return;
    if(!controlling())
    {
      conformity+=0.02f; if(conformity>1)conformity=1;
      return;
    }
    PVector avgNeighborsV = new PVector(0,0);
    for (int i=0; i<neighbors.size(); i++)
    {
      avgNeighborsV.add(neighbors.get(i).velocity);
    }
    avgNeighborsV.div(neighbors.size());
    float mag = abs(avgNeighborsV.mag()-velocity.mag());
    if(mag>3)mag=3;//????????? which number?
    conformity = map(mag, 0, 3, 0.875f, 0);//map in reverse
    //store conformity
    if(lastInd<confs.length-1) 
    {
      confs[lastInd+1]=conformity;
      lastInd++;
    }
    else 
    {
      confs[0] = conformity;
      lastInd = 0;
    }
  }
  
  public void updateDgrControl()
  {
    //find avg conformity in the past second
    float avg = 0;
    for(int i=0; i<confs.length; i++)
    {
      avg += confs[i];
    }
    avg /= confs.length;
    //update degree of control
    dgrControl = 1-avg;
  }
  
  public void bite()//bite and update numFans
  {
    boolean flag = false;
    for(int i=0; i<numFood; i++)
    {
      if(PVector.sub(coord,food[i].coord).mag()<food[i].r+1)
      {
        //It bites on a food!
        flag = true;
        //change color with max alpha
        fillColor = food[i].fillColor;
        strokeColor = fillColor;
        alpha = 255;
        //remove that food and make a new one
        float x = random(-grid.bleed, grid.canvasWidth-grid.bleed);
        float y = random(-grid.bleed, grid.canvasHeight-grid.bleed);
        foodCoordInit[i] = new PVector(x,y);
        food[i] = new Food(foodCoordInit[i].x,foodCoordInit[i].y);
        //change fans status
        numFans += 1+(int)random(3);
      }
    }
    if(!flag)
    {
      float h = hue(fillColor);
      float s = saturation(fillColor);
      float b = brightness(fillColor);
      alpha -= 0.6f; if(alpha<0)alpha=0;
      fillColor = color(h,s,b,alpha);
      strokeColor = lerpColor(color(360),fillColor,map(alpha,0,255,0,1));
    }
    
    if(frameCount%150 == 0 && numFans>0)//150 frames = 6 seconds
    {
      numFans -= 1;
      numFans = (int)(numFans*0.8f+0.5f);
    }
  }
  
  public void updateFans()
  {
    int need = numFans - fans.size();
    if(need<0)//too many fans
    {
      for(int i=0; i<abs(need); i++)
      {
        fans.get(0).idol = null;
        fans.get(0).isFan = false;
        fans.remove(0);
      }
    }
    else if(need>0)//need more fans
    {
      for(int i=0; i<abs(need) && i<neighbors.size(); i++)
      {
        Boid fan = neighbors.get(i);
        fan.isFan = true;
        fan.idol = this;
        fans.add(fan);
      }
    }
  }
  
  public void updateNeighbors()//for individual, only get neighbors that are NOT FANS
  {
    //clear old neighbors list
    neighbors = new ArrayList<Boid>();
    //first get all elems from adjacent grids and store into sqList
    ArrayList<Boid> sqList= new ArrayList<Boid>();
    for(int i=gridR-1; i<=gridR+1; i++)
    {
      //if(i>=0 && i<grid.r)
      for(int j=gridC-1; j<=gridC+1; j++)
      {
        //if(j>=0 && j<grid.c)
        int i2=i; int j2=j;
        if(i<0) i2 += grid.r; else if (i>=grid.r) i2=0;
        if(j<0) j2 += grid.c; else if (j>=grid.c) j2=0;
        for(int k=0; k<grid.object[i2][j2].size(); k++)
        {
          sqList.add(grid.object[i2][j2].get(k));
        }
      }
    }
    //then put elems into neighborList if within distance
    for(int i=0; i<sqList.size(); i++)
    {
      PVector dif = PVector.sub(this.coord, sqList.get(i).coord);
      if (!sqList.get(i).isFan && dif.mag()<grid.gridSize) //change is here
        neighbors.add(sqList.get(i));
    }
  }
  
  public void display()
  {
    displayFood();//display food
    shape.translate(coord.x, coord.y);
    shape.rotate(velocity.heading());
    //fill value
    shape.setFill(fillColor);
    shape.setStroke(strokeColor);
    shape(shape);
    shape.resetMatrix();
  }
  
  public void displayFood()
  {
    for(int i=0; i<numFood; i++)
    {
      fill(food[i].fillColor);
      ellipse(food[i].coord.x, food[i].coord.y,food[i].r,food[i].r);
    }
  } 
  
}
  public void settings() {  size(720,480); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Conformity_branch5_FIN" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
