package teams.student.Goonies;

import components.weapon.economy.Collector;
import objects.entity.unit.Unit;
import org.newdawn.slick.Graphics;
import player.Player;
import teams.student.Goonies.units.*;

public class Goonies extends Player
{
	private static final float PHASE_EARLY = 150f;
	private static final float PHASE_MID = 350f;
	private static final float PHASE_LATE = 600f;

	public void setup()
	{
		setName("Dvij's Disciples");
		setTeamImage("src/teams/student/Goonies/teamLogo.png");
		setTitle("once we start, we finish");
		setColorPrimary(50, 125, 255);
		setColorSecondary(255, 255, 255);
		setColorAccent(255, 255, 255);
	}

	private float mineralsMined()
	{
		return getMineralsMined();
	}

	private boolean phaseStart()
	{
		return mineralsMined() < PHASE_EARLY;
	}

	private boolean phaseEarly()
	{
		return mineralsMined() >= PHASE_EARLY && mineralsMined() < PHASE_MID;
	}

	private boolean phaseMid()
	{
		return mineralsMined() >= PHASE_MID && mineralsMined() < PHASE_LATE;
	}

	private boolean phaseLate()
	{
		return mineralsMined() >= PHASE_LATE;
	}

	private boolean shouldBuildPhantom()
	{
		int count = 0;
		for (Unit u : getUnits(getOpponent(), Unit.class))
		{
			if (u == null || !u.isAlive())
			{
				continue;
			}
			if (!u.hasWeapon(Collector.class))
			{
				continue;
			}
			double distToBase = u.getDistance(getEnemyBase());
			if (distToBase > 400)
			{
				count++;
			}
		}
		return count > 0;
	}

	private int countAssaultTargets()
	{
		int n = 0;
		float workerX = getMyBase().getCenterX();
		float workerY = getMyBase().getCenterY();
		for (Unit u : getUnits(getOpponent(), Unit.class))
		{
			if (u == null || !u.isAlive())
			{
				continue;
			}
			if (u.hasWeapon(Collector.class) || u.hasWeapon(components.weapon.economy.Drillbeam.class))
			{
				continue;
			}
			double toUs = u.getDistance(workerX, workerY);
			double toHim = u.getDistance(getEnemyBase());
			if (toUs < toHim + 500 && toUs < 4000)
			{
				n++;
			}
		}
		return Math.max(n, 1);
	}

	public void strategy() //disgusting
	{
		float mined = mineralsMined();

		if (phaseStart())
		{
			if (countMyUnits(Assault.class) < Math.min(6, countAssaultTargets()))
			{
				buildUnit(Assault.class);
				return;
			}
			if (countMyUnits(Inflictor.class) < 2)
			{
				buildUnit(Inflictor.class);
				return;
			}
			if (getFleetValueUnitPercentage(Gatherer.class) < 0.25f)
			{
				buildUnit(Gatherer.class);
				return;
			}
			if (getFleetValueUnitPercentage(Miner.class) < 0.20f)
			{
				buildUnit(Miner.class);
				return;
			}
			if (shouldBuildPhantom() && getFleetValueUnitPercentage(Parasite.class) < 0.35f)
			{
				buildUnit(Parasite.class);
				return;
			}
			buildUnit(Inflictor.class);
			return;
		}

		if (phaseEarly())
		{
			if (countMyUnits(Assault.class) < Math.min(6, countAssaultTargets()))
			{
				buildUnit(Assault.class);
				return;
			}
			if (getFleetValueUnitPercentage(Gatherer.class) < 0.20f)
			{
				buildUnit(Gatherer.class);
				return;
			}
			if (getFleetValueUnitPercentage(Miner.class) < 0.20f)
			{
				buildUnit(Miner.class);
				return;
			}
			if (countMyUnits(Inflictor.class) >= 4 && countMyUnits(Puller.class) < 1)
			{
				buildUnit(Puller.class);
				return;
			}
			if (shouldBuildPhantom() && getFleetValueUnitPercentage(Parasite.class) < 0.20f)
			{
				buildUnit(Parasite.class);
				return;
			}
			if (getFleetValueUnitPercentage(Inflictor.class) < 0.35f)
			{
				buildUnit(Inflictor.class);
				return;
			}
			buildUnit(Parasite.class);
			return;
		}

		if (phaseMid())
		{
			if (countMyUnits(Assault.class) < Math.min(6, countAssaultTargets()))
			{
				buildUnit(Assault.class);
				return;
			}
			if (getFleetValueUnitPercentage(Gatherer.class) < 0.14f)
			{
				buildUnit(Gatherer.class);
				return;
			}
			if (getFleetValueUnitPercentage(Miner.class) < 0.17f)
			{
				buildUnit(Miner.class);
				return;
			}
			if (countMyUnits(Inflictor.class) >= 4 && countMyUnits(Puller.class) < 2)
			{
				buildUnit(Puller.class);
				return;
			}
			if (shouldBuildPhantom() && getFleetValueUnitPercentage(Parasite.class) < 0.10f)
			{
				buildUnit(Parasite.class);
				return;
			}
			if (getFleetValueUnitPercentage(Inflictor.class) < 0.35f)
			{
				buildUnit(Inflictor.class);
				return;
			}
			buildUnit(Inflictor.class);
			return;
		}

		if (phaseLate())
		{
			if (getFleetValueUnitPercentage(Gatherer.class) < 0.12f)
			{
				buildUnit(Gatherer.class);
				return;
			}
			if (getFleetValueUnitPercentage(Miner.class) < 0.08f)
			{
				buildUnit(Miner.class);
				return;
			}
			if (countMyUnits(Inflictor.class) >= 4 && countMyUnits(Puller.class) < 2)
			{
				buildUnit(Puller.class);
				return;
			}
			buildUnit(Inflictor.class);
		}
	}

	public void draw(Graphics g)
	{
	}
}
