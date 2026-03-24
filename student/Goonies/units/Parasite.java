package teams.student.Goonies.units;

import components.weapon.economy.Collector;
import components.weapon.economy.Drillbeam;
import components.weapon.kinetic.Autocannon;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.Goonies;
import teams.student.Goonies.GooniesUnit;


public class Parasite extends GooniesUnit
{
	private static final float SAFETY = 1.2f;

	@Override
	public void draw(org.newdawn.slick.Graphics g) { }

	public void design()
	{
		setFrame(Frame.LIGHT);
		setModel(Model.STRIKER);
		setStyle(Style.ARROW);
		add(Autocannon.class);
		add(Autocannon.class);
	}

	public void action()
	{
		avoidOutOfBounds();

		Unit gatherer = null;
		double bestD = 1e10;
		for (Unit u : getPlayer().getUnits(getOpponent(), Unit.class))
		{
			if (u == null || !u.isAlive() || !u.hasWeapon(Collector.class))
			{
				continue;
			}
			double d = getDistance(u);
			if (d < bestD)
			{
				bestD = d;
				gatherer = u;
			}
		}

		Unit threat = null;
		float bestT = Float.MAX_VALUE;
		for (Unit u : getPlayer().getUnits(getOpponent(), Unit.class))
		{
			if (u == null || !u.isAlive() || u.hasWeapon(Collector.class))
			{
				continue;
			}
			if (u.hasWeapon(Drillbeam.class))
			{
				continue;
			}
			float t = timeBeforeThreat(u, SAFETY);
			if (t < bestT)
			{
				bestT = t;
				threat = u;
			}
		}

		if (gatherer != null && threat != null)
		{
			float escape = 180f;
			if (threat.getMaxSpeed() < getCurSpeed() && getDistance(threat) >= threat.getMaxRange() * SAFETY)
			{
				double rad = Math.acos(threat.getMaxSpeed() / getCurSpeed());
				escape = 180f - (float) Math.toDegrees(rad);
			}
			float want = getAngleToward(threat.getCenterX(), threat.getCenterY()) + escape;
			float tMove = timeToMove(want);
			if (timeBeforeThreat(threat, SAFETY) <= tMove)
			{
				turnTo(threat);
				turn(escape);
				move();
				attackLowestInRange();
				return;
			}
		}

		if (gatherer != null)
		{
			moveTo(gatherer.getCenterX(), gatherer.getCenterY());
			attackLowestInRange();
			return;
		}

		Unit any = getNearestEnemy();
		if (any != null)
		{
			moveTo(any.getCenterX(), any.getCenterY());
		}
		attackLowestInRange();
	}

	private void attackLowestInRange()
	{
		Unit t = getLowestHealthEnemyInRadius((int) getMaxRange());
		if (t != null && getWeaponOne() != null)
		{
			getWeaponOne().use(t);
		}
		if (t != null && getWeaponTwo() != null)
		{
			getWeaponTwo().use(t);
		}
	}
}
