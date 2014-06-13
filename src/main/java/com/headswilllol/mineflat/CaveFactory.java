package com.headswilllol.mineflat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaveFactory {

	public static Random r = new Random(Terrain.seed);
	public static List<CaveFactory> caveFactories = new ArrayList<CaveFactory>();
	public static List<CaveFactory> deactivate = new ArrayList<CaveFactory>(); // dem CMEs :P

	private Location l;

	public CaveFactory(Location location){
		this.l = location;
		CaveFactory.caveFactories.add(this);
	}

	public void dig(){
		List<Block> surrounding = new ArrayList<Block>();
		if (l.getY() > 0 &&
				Block.isSolid(l.getLevel(), l.getX(), l.getY())){
			surrounding.add(Block.getBlock(l.getLevel(), l.getX(), l.getY() - 1));
		}
		if (l.getY() < Main.world.getChunkHeight() - 2 &&
				Block.isSolid(l.getLevel(), l.getX(), l.getY() + 1)){
			surrounding.add(Block.getBlock(l.getLevel(), l.getX(), l.getY() + 1));
			surrounding.add(Block.getBlock(l.getLevel(), l.getX(), l.getY() + 1));
			surrounding.add(Block.getBlock(l.getLevel(), l.getX(), l.getY() + 1));
		}
		if (l.getX() > Main.world.getChunkCount() / 2 * -Main.world.getChunkLength() &&
				Block.isSolid(l.getLevel(), l.getX() - 1, l.getY())){
			surrounding.add(Block.getBlock(l.getLevel(), l.getX() - 1, l.getY()));
			surrounding.add(Block.getBlock(l.getLevel(), l.getX() - 1, l.getY()));
		}
		if (l.getX() < (Main.world.getChunkCount() / 2 + 1) * Main.world.getChunkLength() - 1 &&
				Block.isSolid(l.getLevel(), l.getX() + 1, l.getY())){
			surrounding.add(Block.getBlock(l.getLevel(), l.getX() + 1, l.getY()));
			surrounding.add(Block.getBlock(l.getLevel(), l.getX() + 1, l.getY()));
		}
		if (surrounding.size() == 0)
			this.deactivate();
		else {
			Block destroy = surrounding.get(r.nextInt(surrounding.size()));
			l.setX(destroy.getX());
			l.setY(destroy.getY());
			destroy.destroy(); // destrol.getY() *overdrive intensifies*
			int chance = (int)((170 - l.getY()) / 2);
			if (r.nextInt(chance) == 0)
				new CaveFactory(l);
		}

	}

	public void deactivate(){
		deactivate.add(this);
	}

}