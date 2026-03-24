package teams.student.Goonies.units;

import components.weapon.utility.SpeedBoost;
import components.weapon.utility.HeavyRepairBeam;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import teams.student.Goonies.GooniesUnit;

public class Mechanic extends GooniesUnit
{
	private Unit followTarget;
	private Unit primaryHealTarget;

	public void design()
	{
		setFrame(Frame.LIGHT);
		setModel(Model.TRANSPORT);
		setStyle(Style.BUBBLE);
		//add(SpeedBoost.class);
		add(HeavyRepairBeam.class);
	}

	public void action()
	{
		avoidOutOfBounds();


//		if (getWeaponOne() instanceof SpeedBoost)
//		{
//			SpeedBoost boost = (SpeedBoost) getWeaponOne();
//			boost.use();
//		}

		primaryHealTarget = updatePrimaryHealTarget(primaryHealTarget);
		followTarget = (primaryHealTarget != null) ? primaryHealTarget : getNearestInflictor();

		if (followTarget != null)
		{
			//follow inflictors.
			if (getDistance(followTarget) > 450)
			{
				smartMoveTo(followTarget);
			}
		}

		//switched to healer, copied over from fixer class (rip)
		if (primaryHealTarget != null && getWeaponTwo() instanceof HeavyRepairBeam)
		{
			HeavyRepairBeam beam = (HeavyRepairBeam) getWeaponTwo();
			if (getDistance(primaryHealTarget) > beam.getMaxRange() * 0.9f)
			{
				moveTo(primaryHealTarget);
			}
			beam.use(primaryHealTarget);
		}

		// repair the lowest health nearby inflcitros
		if (primaryHealTarget == null)
		{
			Unit ally = getLowestHealthInflictor();
			if (ally != null && getWeaponTwo() instanceof HeavyRepairBeam)
			{
				HeavyRepairBeam beam = (HeavyRepairBeam) getWeaponTwo();
				if (getDistance(ally) > beam.getMaxRange() * 0.9f)
				{
					moveTo(ally);
				}
				beam.use(ally);
			}
		}
	}

	private Unit updatePrimaryHealTarget(Unit current)
	{
		if (current != null)
		{

			if (!current.isAlive() || current.getPercentEffectiveHealth() >= 0.98f)

			{
				current = null;
			}
		}

		if (current != null)
		{
			return current;
		}



		Unit best = null;
		double bestScore = -999999;
		for (Unit ally : getAllies())
		{
			if (ally == null || ally == this || !ally.isAlive())
			{
				continue;
			}
			if (ally.getPercentEffectiveHealth() >= 0.98f)
			{
				continue;
			}

			double score = 0;
			score += (1.0 - ally.getPercentEffectiveHealth()) * 10000;
			score -= getDistance(ally) * 0.25;

			if (ally instanceof Inflictor)
			{
				score += 2500;
			}


			//downdoot other workers

			if (isWorker(ally))
			{
				score -= 800;
			}

			if (score > bestScore)
			{
				bestScore = score;
				best = ally;
			}
		}
		return best;
	}

	private Unit getNearestInflictor()
	{
		Unit best = null;
		double bestDist = Double.MAX_VALUE;
		for (Unit ally : getAllies())
		{
			if (ally == null || !ally.isAlive() || !(ally instanceof Inflictor))
			{
				continue;
			}
			double d = getDistance(ally);
			if (d < bestDist)
			{
				bestDist = d;
				best = ally;
			}
		}
		return best;
	}

	private Unit getLowestHealthInflictor()
	{
		Unit best = null;
		double bestHealth = 1.01;

		for (Unit ally : getAllies())
		{
			if (ally == null || ally == this || !ally.isAlive() || !(ally instanceof Inflictor))
			{
				continue;
			}
			double pct = ally.getPercentEffectiveHealth();

			if (pct < bestHealth)
			{
				bestHealth = pct;
				best = ally;
			}
		}
		return best;
	}

	@Override
	public void draw(Graphics g)
	{
		g.setColor(Color.orange);
		g.setLineWidth(1);


		if (primaryHealTarget != null && primaryHealTarget.isAlive())
		{
			g.setColor(Color.magenta);
			g.drawLine(getCenterX(), getCenterY(), primaryHealTarget.getCenterX(), primaryHealTarget.getCenterY());
		}

		g.setColor(Color.orange);
		if (followTarget != null && followTarget.isAlive())
		{
			g.drawLine(getCenterX(), getCenterY(), followTarget.getCenterX(), followTarget.getCenterY());
		}
	}
}

