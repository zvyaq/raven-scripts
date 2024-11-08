final Color Cherry1 = new Color(243, 58, 106);
final Color Cherry2 = new Color(253, 178, 185);

final Color CottonCandy1 = new Color(135, 215, 243);
final Color CottonCandy2 = new Color(254, 104, 204);

final Color Flare1 = new Color(241, 39, 17);
final Color Flare2 = new Color(244, 169, 24);

final Color Flower1 = new Color(211, 91, 231);
final Color Flower2 = new Color(214, 158, 231);

final Color Gold1 = new Color(254, 252, 193);
final Color Gold2 = new Color(255, 250, 53);

final Color GreyScale1 = new Color(116, 116, 116);
final Color GreyScale2 = new Color(186, 186, 186);

final Color Royal1 = new Color(109, 182, 229);
final Color Royal2 = new Color(33, 73, 166);

final Color Sky1 = new Color(44, 220, 247);
final Color Sky2 = new Color(139, 253, 249);

final Color Vine1 = new Color(28, 255, 49);
final Color Vine2 = new Color(171, 255, 172);

Color[][] accents = {
   {null, null},
   {Cherry1, Cherry2},
   {CottonCandy1, CottonCandy2},
   {Flare1, Flare2},
   {Flower1, Flower2},
   {Gold1, Gold2},
   {GreyScale1, GreyScale2},
   {Royal1, Royal2},
   {Sky1, Sky2},
   {Vine1, Vine2}
};

final float astolfoEndX = 150;
final float astolfoEndY = 50;

Color accent = new Color(0, 0, 0, 255);

float adjustedX, adjustedY;
int x, y;
int dragX = 963;
int dragY = 565;
int firstX, firstY;
boolean track = false;
boolean firstClick = true;
float timeMultiplier, offset, rangeSwing, followPlayerOffsetY, blurBackgroundRadius, borderRadius, outerBorderRadius;
boolean traditionHealthColor, followPlayer, showHudOnlyOnSwing, blurBackground, inChatOF;
int theme, borderRadiusPercent, screenFollowPlayerOffsetX, screenFollowPlayerOffsetY, backgroundBrightness, backgroundOpacity, blurBackgroundPasses, background;

void onLoad() {
   modules.registerDescription("Visual settings:");
   modules.registerSlider("Border radius", "%", 25, 0, 50, 0.5);
   modules.registerSlider("Background brightness", "%", 0, 0, 100, 1);
   modules.registerSlider("Background opacity", "", 150, 0, 255, 1);
   modules.registerButton("Blur background", true);
   modules.registerDescription(" - Blur background settings:");
   modules.registerSlider("Passes", "", 5, 0, 10, 1);
   modules.registerSlider("Radius", "", 5, 0, 10, 1);
   modules.registerDescription("Behavior settings:");
   modules.registerButton("Show hud only on swing", true);
   modules.registerButton("Follow player", false);
   modules.registerDescription(" - Follow player settings:");
   modules.registerSlider("Screen x offset", "px", 0, -200, 200, 10);
   modules.registerSlider("Screen y offset", "px", 0, -200, 200, 10);
   modules.registerSlider("Y offset", "blocks", -0.5, -5, 5, 0.5);
   if (config.get("dragX") != null || config.get("dragY") != null) {
      dragX = (int) Integer.parseInt(config.get("dragX")); x = dragX;
      dragY = (int) Integer.parseInt(config.get("dragY")); y = dragY;
   }
   x = dragX;
   y = dragY;
   adjustedX = x / 2;
   adjustedY = y / 2;
   modules.registerDescription("                     (zvyaq)");
}

void onEnable() {
   updateComponents();
   updatePaint();
}


void onPreUpdate() {
    inChatOF = client.getScreen().contains("Chat");
    int ticks = client.getPlayer().getTicksExisted();
    if (ticks % 5 == 0) {updateComponents(); updatePaint();}
}

void updateComponents() {
   // buttons
   blurBackground = modules.getButton(scriptName, "Blur background");
   traditionHealthColor = modules.getButton("TargetHUD", "Traditional health color");
   followPlayer = modules.getButton(scriptName, "Follow player");
   showHudOnlyOnSwing = modules.getButton(scriptName, "Show hud only on swing");
   // sliders
   offset = (float) modules.getSlider("Settings", "Offset");
   timeMultiplier = (float) modules.getSlider("Settings", "Time multiplier");
   rangeSwing = (float) modules.getSlider("KillAura", "Range (swing)");
   blurBackgroundRadius = (float) modules.getSlider(scriptName, "Radius");
   blurBackgroundPasses = (int) modules.getSlider(scriptName, "Passes");
   theme = (int) modules.getSlider("TargetHUD", "Theme");
   borderRadiusPercent = (int) modules.getSlider(scriptName, "Border radius");
   backgroundBrightness = (int) modules.getSlider(scriptName, "Background brightness");
   backgroundOpacity = (int) modules.getSlider(scriptName, "Background opacity");
   if (followPlayer){
   screenFollowPlayerOffsetX = (int) modules.getSlider(scriptName, "Screen x offset");
   screenFollowPlayerOffsetY = (int) modules.getSlider(scriptName, "Screen y offset");
   followPlayerOffsetY = (float) modules.getSlider(scriptName, "Y offset");
   }
}

void updatePaint() {
    borderRadius = 7f * borderRadiusPercent / 100;
    outerBorderRadius = borderRadius + 3f;
    Color backgroundHSB = Color.getHSBColor(0, 0, backgroundBrightness / 100f);
    background = new Color(backgroundHSB.getRed(), backgroundHSB.getGreen(), backgroundHSB.getBlue(), backgroundOpacity).getRGB();
}

void onRenderTick(float partialTicks){
   //return if screen not empty
   if (!client.getScreen().isEmpty() && !inChatOF) return;
   
   //killaura vars
   Entity entity = modules.getKillAuraTarget();
   Entity self = client.getPlayer();

   //render
   float renderedHudEndX = astolfoEndX;
   float renderedHudEndY = astolfoEndY;
   
   //drag logic

   if (!followPlayer && inChatOF) dragLogic((int)renderedHudEndX * 2, (int)renderedHudEndY * 2); else if (!inChatOF) track = false;
   
   //render

   if (inChatOF || entity != null) {
      if (inChatOF) entity = self; else
      if (showHudOnlyOnSwing && entity != null && self.getPosition().distanceTo(entity.getPosition()) - 0.5 >= rangeSwing) return;
      
      //themes
      accent = traditionHealthColor ? getHealthColor(entity) : (theme == 0) ? getRainbow(1) : (theme > 0 && theme < accents.length) ? blendColors(accents[theme][0], accents[theme][1], 1) : new Color(0, 0, 0, 255);

      //follow player

      if(followPlayer) {
        if (!render.isInView(entity)) return;
         followPlayer(renderedHudEndX, renderedHudEndY, entity, partialTicks);
      } else if (dragX != x || dragY != y) x = dragX; y = dragY; adjustedX = x / 2; adjustedY = y / 2;
      drawAstolfo(entity);
   }
}

void drawAstolfo(Entity entity){
      if (borderRadius != 0) {
      if (blurBackground) {
      render.blur.prepare();
      render.roundedRect(adjustedX, adjustedY, astolfoEndX + adjustedX, astolfoEndY + adjustedY, outerBorderRadius, -1);
      render.blur.apply(blurBackgroundPasses, blurBackgroundRadius);}
      render.roundedRect(adjustedX, adjustedY, astolfoEndX + adjustedX, astolfoEndY + adjustedY, outerBorderRadius, background);
      render.roundedRect(30 + adjustedX, 40 + adjustedY, 117.5f + 29.5f + adjustedX, 7.5f + 39.5f + adjustedY, borderRadius, new Color(clamp(accent.getRed() - 195, 0, 255), clamp(accent.getGreen() - 195, 0, 255), clamp(accent.getBlue() - 195, 0, 255), 255).getRGB());
      render.roundedRect(30 + adjustedX, 40 + adjustedY, 29.5f + adjustedX + (entity.getHealth() / entity.getMaxHealth()) * 117.5f, 7.5f + 39.5f + adjustedY, borderRadius, new Color(clamp(accent.getRed() - 77, 0, 255), clamp(accent.getGreen() - 77, 0, 255), clamp(accent.getBlue() - 77, 0, 255), 255).getRGB());   
      render.roundedRect(30 + adjustedX, 40 + adjustedY, clampFloat((29.5f + adjustedX + (entity.getHealth() / entity.getMaxHealth()) * 117.5f) - 5, 30 + adjustedX, adjustedX + astolfoEndX), 7.5f + 39.5f + adjustedY, borderRadius, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 255).getRGB());
      } else { //no border radius
      if (blurBackground) {
      render.blur.prepare();
      render.rect(adjustedX, adjustedY, astolfoEndX + adjustedX, astolfoEndY + adjustedY, -1);
      render.blur.apply(blurBackgroundPasses, blurBackgroundRadius);}
      render.rect(adjustedX, adjustedY, astolfoEndX + adjustedX, astolfoEndY + adjustedY, background);
      render.rect(30 + adjustedX, 40 + adjustedY, 117.5f + 29.5f + adjustedX, 7.5f + 39.5f + adjustedY, new Color(clamp(accent.getRed() - 195, 0, 255), clamp(accent.getGreen() - 195, 0, 255), clamp(accent.getBlue() - 195, 0, 255), 255).getRGB());
      render.rect(30 + adjustedX, 40 + adjustedY, 29.5f + adjustedX + (entity.getHealth() / entity.getMaxHealth()) * 117.5f, 7.5f + 39.5f + adjustedY, new Color(clamp(accent.getRed() - 77, 0, 255), clamp(accent.getGreen() - 77, 0, 255), clamp(accent.getBlue() - 77, 0, 255), 255).getRGB());   
      render.rect(30 + adjustedX, 40 + adjustedY, clampFloat((29.5f + adjustedX + (entity.getHealth() / entity.getMaxHealth()) * 117.5f) - 5, 30 + adjustedX, adjustedX + astolfoEndX), 7.5f + 39.5f + adjustedY, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 255).getRGB());
      };
      render.text(formatDoubleStr((double) Math.round(10 * entity.getHealth() / 2) / 10), 30 + adjustedX, 17.5f + adjustedY, 2, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 255).getRGB(), true);
      render.text(entity.getName(), 30 + adjustedX, 5 + adjustedY, 1, 0xFFFFFFFF, true);
      //gl.scissor(true);
      //gl.scissor((int)adjustedX * 2, (client.getDisplaySize()[1] - (int)adjustedY) * 2 - (int)astolfoEndY * 2, 60 - 1, (int)astolfoEndY * 2 - 7);
      render.entityGui(entity, 10 + 5 + (int)adjustedX, 40 + 5 + (int)adjustedY, -200, 0, 20);
      //gl.scissor(false);
      if (track && borderRadius != 0) {
         drawRoundedRectOutline(adjustedX, adjustedY, 150 + adjustedX, 50 + adjustedY, outerBorderRadius, 1, 0x96FFFFFF);
      } else if (track){ drawRectOutline(adjustedX, adjustedY, 150 + adjustedX, 50 + adjustedY, 1, 0x96FFFFFF);}
}


void dragLogic(int offsetX, int offsetY) {
int[] displaySize = client.getDisplaySize();
   if (keybinds.isMouseDown(0) && firstClick) {
      int[] position = keybinds.getMousePosition();
      position[1] = displaySize[1] * 2 - position[1];
      firstX = position[0];
      firstY = position[1];
      firstClick = false;
   if (x <= firstX && firstX <= x + offsetX && y <= firstY && firstY <= y + offsetY) track = true;
   }
   if (!keybinds.isMouseDown(0)) {
      firstClick = true;
      track = false;
   }
   if (track) {
      int[] position = keybinds.getMousePosition();
      position[1] = displaySize[1] * 2- position[1];
      int deltaX = position[0] - firstX;
      int deltaY = position[1] - firstY;
      dragX = dragX + deltaX;
      dragY = dragY + deltaY;
      x = dragX;
      y = dragY;
      adjustedX = x / 2;
      adjustedY = y / 2;
      firstX = firstX + deltaX;
      firstY = firstY + deltaY;
      config.set("dragX", Integer.toString(dragX));
      config.set("dragY", Integer.toString(dragY));
   }
}

Color getHealthColor(Entity entity) {
   return 
   (entity.getHealth() / entity.getMaxHealth() >= 0.75) ? new Color(3, 213, 2) : 
   (entity.getHealth() / entity.getMaxHealth() >= 0.5) ? new Color(212, 212, 1) : 
   (entity.getHealth() / entity.getMaxHealth() <= 0.25) ? new Color(229, 2, 1) : 
   (entity.getHealth() / entity.getMaxHealth() >= 0.25) ? new Color(212, 167, 1) : 
   new Color(0,0,0);
}

void followPlayer(float renderedHudEndX, float renderedHudEndY, Entity entity, float partialTicks) { // winnie
   Vec3 position = entity.getPosition();
   Vec3 lastPosition = entity.getLastPosition();
   position.x = interpolate(position.x, lastPosition.x, partialTicks);
   position.y = interpolate(position.y, lastPosition.y, partialTicks);
   position.z = interpolate(position.z, lastPosition.z, partialTicks);
   double heightOffset = position.y + (!entity.isSneaking() ? entity.getHeight() : entity.getHeight() - 0.25) + followPlayerOffsetY;
   Vec3 screen = render.worldToScreen(position.x, heightOffset, position.z, client.getDisplaySize()[2], partialTicks);
   double distSq = entity.getPosition().distanceToSq(client.getPlayer().getPosition());
   adjustedX = (float)screen.x - renderedHudEndX / 2 + screenFollowPlayerOffsetX;
   adjustedY = (float)screen.y - renderedHudEndY / 4 + screenFollowPlayerOffsetY;
   x = (int)(adjustedX * 2);
   y = (int)(adjustedY * 2);
}

double interpolate(double current, double old, float scale) { // winnie
    return old + (current - old) * scale;
}

void drawRectOutline(float x1, float y1, float x2, float y2, float width, int color) { // pug
    render.line2D(x1, y1, x2, y1, width, color);
    render.line2D(x1, y2, x2, y2, width, color);
    render.line2D(x1, y1, x1, y2, width, color);
    render.line2D(x2, y1, x2, y2, width, color);
}


void drawRoundedRectOutline(float x1, float y1, float x2, float y2, float radius, float width, int color) { // pug
    if (x1 > x2) {
        float temp = x1;
        x1 = x2;
        x2 = temp;
    }
    if (y1 > y2) {
        float temp = y1;
        y1 = y2;
        y2 = temp;
    }

    float rectX1 = x1 + radius;
    float rectY1 = y1 + radius;
    float rectX2 = x2 - radius;
    float rectY2 = y2 - radius;

    render.line2D(rectX1, y1, rectX2, y1, width, color);
    render.line2D(rectX1, y2, rectX2, y2, width, color);
    render.line2D(x1, rectY1, x1, rectY2, width, color);
    render.line2D(x2, rectY1, x2, rectY2, width, color);

    double degree = Math.PI / 180;
    for (int corner = 0; corner < 4; corner++) {
        double centerX = (corner < 2) ? rectX2 : rectX1;
        double centerY = (corner % 3 == 0) ? rectY2 : rectY1;

        double startAngle = 90 * corner;
        double endAngle = startAngle + 90;

        int segments = (int) (endAngle - startAngle);
        for (int i = 0; i < segments; i++) {
            double angle1 = (startAngle + i) * degree;
            double angle2 = (startAngle + i + 1) * degree;

            double xStart = centerX + Math.sin(angle1) * radius;
            double yStart = centerY + Math.cos(angle1) * radius;
            double xEnd = centerX + Math.sin(angle2) * radius;
            double yEnd = centerY + Math.cos(angle2) * radius;

            render.line2D(xStart, yStart, xEnd, yEnd, width, color);
        }
    }
}

Color getRainbow(int i) { // pug
   float hue = ((client.time() + i * (int) (10 * offset))  % (int) ( 15000 / timeMultiplier)) / (float) ( 15000 / timeMultiplier); 
   return Color.getHSBColor(hue, 1f, 1f);
}

double getWaveRatio(int i) { // pug
   float time = ((client.time() + i * (int) (10 * offset)) % (int) (3000 / timeMultiplier)) / (float) (3000 / timeMultiplier);
   double waveRatio = (time <= 0.5) ? (time * 2) : (2 - time * 2);
   return waveRatio;
}

Color blendColors(Color color1, Color color2, int i) { // pug 
   double ratio = getWaveRatio(i);
   int r = clamp((int) (color1.getRed() * ratio + color2.getRed() * (1 - ratio)), 0, 255);
   int g = clamp((int) (color1.getGreen() * ratio + color2.getGreen() * (1 - ratio)), 0, 255);
   int b = clamp((int) (color1.getBlue() * ratio + color2.getBlue() * (1 - ratio)), 0, 255);
   return new Color(r, g, b);
}

int clamp(int val, int min, int max) { // pug
   return (val < min) ? min : (val > max) ? max : val;
}

float clampFloat(float val, float min, float max) { // pug
   return (val < min) ? min : (val > max) ? max : val;
}

String formatDoubleStr(double val) { // pug
    return val == (long) val ? Long.toString((long) val) : Double.toString(val);
}
