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
  color fillColor;
  color strokeColor;
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
  
  void run()
  {
    update();
    display();
  }
  
  void update()
  {
    grid.removeElem(this);
    updateFans();
    updateNeighbors();
    //from flow
    PVector separation = sepVector().mult(3);
    PVector cohesion = cohVector().mult(0.25);
    PVector alignment = aliVector().mult(0.4);
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
  
  PVector ctrVector()
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
    return steer.mult(1.2);//weight
  }
  
  boolean controlling()
  {
    if(keyPressed && key==CODED)
    {
      if(keyCode==UP || keyCode==DOWN ||
           keyCode==LEFT|| keyCode==RIGHT)
        return true;
    }
    return false;
  }

  
  void updateConformity()
  {
    if(neighbors.size()==0)return;
    if(!controlling())
    {
      conformity+=0.02; if(conformity>1)conformity=1;
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
    conformity = map(mag, 0, 3, 0.875, 0);//map in reverse
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
  
  void updateDgrControl()
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
  
  void bite()//bite and update numFans
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
      alpha -= 0.6; if(alpha<0)alpha=0;
      fillColor = color(h,s,b,alpha);
      strokeColor = lerpColor(color(360),fillColor,map(alpha,0,255,0,1));
    }
    
    if(frameCount%150 == 0 && numFans>0)//150 frames = 6 seconds
    {
      numFans -= 1;
      numFans = (int)(numFans*0.8+0.5);
    }
  }
  
  void updateFans()
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
  
  void updateNeighbors()//for individual, only get neighbors that are NOT FANS
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
  
  void display()
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
  
  void displayFood()
  {
    for(int i=0; i<numFood; i++)
    {
      fill(food[i].fillColor);
      ellipse(food[i].coord.x, food[i].coord.y,food[i].r,food[i].r);
    }
  } 
  
}