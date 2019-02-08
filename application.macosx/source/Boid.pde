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
  color rgb;
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
    this.speed0 = random(3.95,4.05);
    float tmp = random(TWO_PI);
    this.velocity = new PVector(cos(tmp),sin(tmp));
    this.force0 = random(0.029,0.032);
    this.acceleration = new PVector(0,0);
    updateNeighbors();
    this.shape = makeShape();
  }

  void update()//checked
  {
    grid.removeElem(this);
    updateNeighbors();
    if(isFan) conformity = idol.conformity;
    else conformity = 1;
    PVector separation = sepVector().mult(3);
    PVector cohesion = cohVector().mult(0.25);
    PVector alignment = aliVector().mult(0.4);
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
  
  void display()//checked
  {
    shape.translate(coord.x, coord.y);
    shape.rotate(velocity.heading());
    if (isFan) shape.setStroke(idol.strokeColor);
    else shape.setStroke(360);
    shape(shape);
    shape.resetMatrix();
  }
  
  //----
  
  PShape makeShape()
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

  void updateNeighbors()//checked
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
  PVector aliVector()
  {
    PVector avg;
    if(isFan) avg = idol.velocity;
    else avg = avgNeighborsVelocity();
    //turn this boid's velocity toward the average
    PVector dif = PVector.sub(avg,velocity);
    if(!isFan)dif.limit(force0);
    return dif;
  }
  
  PVector cohVector()
  {
    PVector avg;
    if(isFan) avg = idol.coord;
    else avg = avgNeighborsPosition();
    //turn it toward the center of its neighbors
    return moveTo(avg).limit(force0 * 8);
  }
  
  PVector sepVector()//change into taking a variable
  {
    PVector steer = new PVector(0,0);
    int count = 0;
    repelZone = grid.gridSize* (0.2+(1-conformity)*0.8);//aware zone linearly related to conformity
    for(int i=0; i<neighbors.size(); i++)
    {
      //check distance with each neighbor
      PVector dif = PVector.sub(coord,neighbors.get(i).coord);
      if(dif.mag()>0 && dif.mag()<neighbors.get(i).repelZone)//enters neighbor's repel zone
      {
        //add all "to move away" vectors to steer
        //PVector tmp = dif.copy(); tmp.normalize(); tmp.setMag(sqrt(tmp.mag()));
        //dif.setMag(tmp.mag()*dif.mag());
        dif.mult(1.5);//arbitrary var
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
      steer.limit(force0*1.5);
    }
    return steer;
  }
  
  //----helpers----
  
  void moveToward(PVector target)//checked
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
  
  PVector moveTo(PVector target)
  {
    PVector difCoord = PVector.sub(target,coord);
    difCoord.setMag(speed0);
    PVector difVel = PVector.sub(difCoord,velocity);
    if(!isFan)difVel.limit(force0);
    return difVel;
  }
  
  PVector avgNeighborsVelocity()//checked
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
  
  PVector avgNeighborsPosition()//checked
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
  
  void borders(PVector vec) {
    while (vec.x < -grid.bleed) vec.x += grid.canvasWidth;
    while (vec.y < -grid.bleed) vec.y += grid.canvasHeight;
    while (vec.x > grid.canvasWidth) vec.x -= grid.canvasWidth;
    while (vec.y > grid.canvasHeight) vec.y -= grid.canvasHeight;
  }
  
  boolean out(PVector vec)
  {
    if(vec.x < -grid.bleed || vec.x > grid.canvasWidth ||
       vec.y < -grid.bleed || vec.y > grid.canvasHeight)
      return true;
    return false;
  }
}