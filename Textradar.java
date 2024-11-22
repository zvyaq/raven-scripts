List<Map<String, Object>> players = new ArrayList<>();
int showedPlayersCount, showDistance, offsetMultiplier;
float timeMultiplier, offset;

void onLoad() {
    modules.registerSlider("Showed players", "", 10, 1, 20, 1);
    modules.registerSlider("Distance", "m", 25, 1, 40, 1);
    modules.registerSlider("Offset multiplier", "", -5, -10, 10, 1);
}

void onEnable() {
    updateComponents();
}

void updateComponents() {
    showedPlayersCount = (int) modules.getSlider(scriptName, "Showed players");
    showDistance = (int) modules.getSlider(scriptName, "Distance");
    offset = (float) modules.getSlider("Settings", "Offset");
    timeMultiplier = (float) modules.getSlider("Settings", "Time multiplier");
    offsetMultiplier = (int) modules.getSlider(scriptName, "Offset multiplier");
}

void onPreUpdate() {
    if (client.getPlayer().getTicksExisted() % 5 == 0) {
        updateComponents();
    };
    Entity self = client.getPlayer();
    players.clear();
    for (Entity player : client.getWorld().getPlayerEntities()) {
        if (self.equals(player)) continue;
        int distance = (int) Math.round(self.getPosition().distanceTo(player.getPosition()));
        if (distance > showDistance) continue;
        float ratioDistance = distance * 1f / showDistance;
        int distanceColor = (ratioDistance >= 0.75) ? 0xFF03D502 :
                (ratioDistance >= 0.5) ? 0xFFD4D401 :
                (ratioDistance <= 0.25) ? 0xFFE50201 :
                (ratioDistance >= 0.25) ? 0xFFD4A701 :
                0;
        Map<String, Object> object = new HashMap<>();
        object.put("name", player.getName());
        object.put("distance", distance);
        object.put("distanceText", distance + "m");
        object.put("distanceColor", distanceColor);
        players.add(object);
    };
    players.sort((p1, p2) -> Integer.compare((int) p2.get("distance"), (int) p1.get("distance")));
    int start = Math.max(players.size() - showedPlayersCount, 0);
    players = new ArrayList<>(players.subList(start, players.size()));
    players.sort((p1, p2) -> Integer.compare((int) p1.get("distance"), (int) p2.get("distance")));
}

void onRenderTick(float partialTicks) {
    if (!client.getScreen().isEmpty()) return;
    int index = 0;
    drawRainbowRect(6, 8, 200, 9);
    render.rect(6, 10 - 1, 200, 10 + 10 - 1, 0xFF141414);
    for (Map<String, Object> p : players) {
        String playerName = (String) p.get("name");
        String distanceText = (String) p.get("distanceText");
        int distanceColor = (int) p.get("distanceColor");
        int background = (index % 2 == 0) ? 0xFF141414 : 0xFF1A1A1A;
        if (index != 0) render.rect(6, 10 + 10 * index - 1, 200, 10 + 10 * index + 10 - 1, background);
        render.text(playerName, 10, 10 + 10 * index, 1, 0xFFFFFFFF, true);
        render.text(distanceText, 10 + 187 - render.getFontWidth(distanceText), 10 + 10 * index, 1, distanceColor, true);
        index++;
    };
}
// OMG SKIDDED CODE!!!!!!!!
Color getRainbow(int i) { // pug
    float hue = ((client.time() + i * (int)(offsetMultiplier * offset)) % (int)(15000 / timeMultiplier)) / (float)(15000 / timeMultiplier);
    return Color.getHSBColor(hue, 0.7f, 0.8f);
}

void drawRainbowRect(float x1, float y1, float x2, float y2) {
    gl.push();
    gl.texture2d(false);
    gl.begin(0);
    for (float yB = y1; yB < y2; yB = yB + 0.5f) {
        for (float xB = x1; xB < x2; xB = xB + 0.5f) {
            Color color12 = new Color(0, 0, 0, 255);
            color12 = getRainbow((int) (xB * 10));
            gl.color(color12.getRed() / 255f, color12.getGreen() / 255f,
            color12.getBlue() / 255f, 1);
            gl.vertex2(xB, yB + 0.5f);
        }
    }
    gl.end();
    gl.color(1, 1, 1, 1);
    gl.texture2d(true);
    gl.pop();
}
