boolean firstC0F = true;

void onWorldJoin(Entity entity){
    if (entity == client.getPlayer()) firstC0F = true;
}

boolean onPacketSent(CPacket packet) {
    if (packet instanceof C0F) {
		if(client.getPlayer().isDead()) firstC0F = true;
		if(!firstC0F) { return false;}
        firstC0F = false;
	} else 
    if (packet instanceof C0B) {
	    return false;
    }
    return true;
}