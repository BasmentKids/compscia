package teams.student.Goonies.units;

import components.upgrade.Shield;
import components.weapon.energy.Laser;
import components.weapon.kinetic.Autocannon;
import components.weapon.utility.SpeedBoost;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;


public class Assault extends GooniesUnit
{
	@Override
	public void draw(org.newdawn.slick.Graphics g) { }

	public void design()

	{


		setFrame(Frame.MEDIUM);
		setModel(Model.STRIKER);

		setStyle(Style.ARROW);
		//add(SpeedBoost.class);
		add(Autocannon.class);
		add(Laser.class);
		add(Shield.class);
	}

	public void action()
	{

		if (getWeaponOne() instanceof SpeedBoost)

		{
			((SpeedBoost) getWeaponOne()).use();
		}

		Unit target = getNearestAllyGatherer();

		if (target == null)
		{
			target = getFarthestWorker();
		}

		avoidOutOfBounds();
		if (target != null)
		{
			moveTo(target.getCenterX(), target.getCenterY());
		}
		else
		{
			moveTo(getHomeBase().getCenterX(), getHomeBase().getCenterY());
		}

		Unit enemy = getLowestHealthEnemyInRadius((int) getMaxRange());
		if (enemy != null && getWeaponTwo() != null)
		{
			getWeaponTwo().use(enemy);
		}
	}

	private Unit getNearestAllyGatherer()
	{

		Unit best = null;
		double bestD = 1e10;
		for (Unit u : getAllies())
		{

			if (u==null || !u.isAlive() || !(u instanceof Gatherer))
			{

				continue;
			}
			double d = getDistance(u);
			if (d < bestD)
			{
				bestD=d;
				best=u;
			}
		}
		return best;
	}

	private Unit getFarthestWorker()
	{
		Unit best = null;
		double bestD = -1;
		for (Unit u : getAllies())
		{
			if (u == null || !u.isAlive())
			{
				continue;
			}
			if (!(u instanceof Gatherer) && !(u instanceof Miner))
			{
				continue;
			}
			double d = u.getDistance(getEnemyBase());
			if (d > bestD)
			{
				bestD = d;
				best = u;
			}
		}
		return best;
	}
}
