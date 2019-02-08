
class Food
{
  float r;
  PVector coord;
  color fillColor;
  
  Food(float x, float y)
  {
    this.r = random(8,20);
    this.coord = new PVector(x,y);
    this.fillColor = color(random(360),random(60,70),random(75,90),180);
  }
  
}