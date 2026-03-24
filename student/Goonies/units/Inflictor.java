package teams.student.Goonies.units;

import components.upgrade.Munitions;
import components.weapon.Weapon;
import components.weapon.kinetic.Autocannon;
import components.weapon.kinetic.HeavyAutocannon;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;

public class Inflictor extends GooniesUnit
{
	private static final float MIN_SPACE_FROM_ALLY_INFLICTOR = 300f;

	private float randomY;

	@Override
	public void draw(org.newdawn.slick.Graphics g) { }

	public void design()
	{
		setFrame(Frame.ASSAULT);
		setModel(Model.DESTROYER);
		setStyle(Style.CANNON);
		add(Autocannon.class);
		add(HeavyAutocannon.class);
		add(Munitions.class);
		randomY = (float) (Math.random() * 400);
		if (Math.random() > 0.5)
		{
			randomY = -randomY;
		}
	}

	public void action()
	{
		avoidOutOfBounds();

		Unit enemy = getNearestEnemy();
		if (enemy == null)
		{
			enemy = getEnemyBase();
		}

		//this is push when few and enemy is there
		boolean push = countInflictors() >= 2 && enemy != null && enemy.isAlive() && getDistance(enemy) < 5000;

		if (push && enemy != null)
		{
			if (getDistance(enemy) > getMaxRange() * 0.85f)
			{
				moveTo(enemy.getCenterX(), enemy.getCenterY() + randomY);
			}
			else
			{
				if (!separateFromOtherInflictors())
				{
					turnTo(enemy);
					turnAround();
					move();
				}
			}
		}
		else
		{
			float[] avg = getAvgInflictorPos();
			circleAroundTarget(avg[0], avg[1], 200);
		}

		Unit shoot = getLowestHealthEnemyInRadius((int) getMaxRange());
		if (shoot != null)
		{
			Weapon w1 = getWeaponOne();
			Weapon w2 = getWeaponTwo();
			if (w1 != null && getDistance(shoot) <= w1.getMaxRange())
			{
				w1.use(shoot);
			}
			if (w2 != null && getDistance(shoot) <= w2.getMaxRange())
			{
				w2.use(shoot);
			}
		}
	}

	private boolean separateFromOtherInflictors()
	{
		float ax = 0f;
		float ay = 0f;
		for (Unit u : getAllies())
		{
			if (u == null || !u.isAlive() || u == this || !(u instanceof Inflictor))
			{
				continue;
			}
			float dx = getCenterX() - u.getCenterX();
			float dy = getCenterY() - u.getCenterY();
			float d = (float) Math.sqrt(dx * dx + dy * dy);
			if (d < MIN_SPACE_FROM_ALLY_INFLICTOR && d > 0.01f)
			{
				ax += dx / d;
				ay += dy / d;
			}
		}
		float len = (float) Math.sqrt(ax * ax + ay * ay);
		if (len < 0.01f)
		{
			return false;
		}
		float step = 220f;
		moveTo(getCenterX() + ax / len * step, getCenterY() + ay / len * step);
		return true;
	}

	private int countInflictors()
	{
		int n = 0;
		for (Unit u : getAllies())
		{
			if (u != null && u.isAlive() && u instanceof Inflictor)
			{
				n++;
			}
		}
		return n;
	}

	private float[] getAvgInflictorPos()
	{
		float x = 0;
		float y = 0;
		int n = 0;
		for (Unit u : getAllies())
		{
			if (u == null || !u.isAlive() || !(u instanceof Inflictor))
			{
				continue;
			}
			x += u.getCenterX();
			y += u.getCenterY();
			n++;
		}
		if (n == 0)
		{
			return new float[] { getCenterX(), getCenterY() };
		}
		return new float[] { x / n, y / n };
	}
}
