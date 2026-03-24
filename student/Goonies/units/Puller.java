package teams.student.Goonies.units;

import components.upgrade.HeavyMunitions;
import components.weapon.utility.Pullbeam;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import teams.student.Goonies.GooniesUnit;


public class Puller extends GooniesUnit
{
	@Override
	public void draw(org.newdawn.slick.Graphics g) { }

	public void design()
	{
		setFrame(Frame.HEAVY);
		setModel(Model.ARTILLERY);
		setStyle(Style.WEDGE);
		add(Pullbeam.class);
		add(HeavyMunitions.class);
	}

	public void action()
	{
		avoidOutOfBounds();

		float ax = 0;
		float ay = 0;
		int n = 0;
		for (Unit u : getAllies())
		{
			if (u == null || !u.isAlive() || !(u instanceof Inflictor))
			{
				continue;
			}
			ax += u.getCenterX();
			ay += u.getCenterY();
			n++;
		}
		if (n > 0)
		{
			moveTo(ax / n, ay / n);
		}
		else
		{
			moveTo(getHomeBase().getCenterX(), getHomeBase().getCenterY());
		}

		Unit pull = getLowestHealthEnemyInRadius((int) getMaxRange());
		if (pull != null && getWeaponOne() != null)
		{
			getWeaponOne().use(pull);
		}
	}
}
