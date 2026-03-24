package teams.student.Goonies.units;

import components.weapon.Weapon;
import components.weapon.economy.Collector;
import objects.entity.unit.Frame;
import objects.entity.unit.Model;
import objects.entity.unit.Style;
import objects.entity.unit.Unit;
import objects.resource.Resource;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import teams.student.Goonies.GooniesUnit;

public class Gatherer extends GooniesUnit
{
	private Resource targetResource;
	private boolean returning;

	public void design()
	{
		setFrame(Frame.LIGHT);
		setModel(Model.TRANSPORT);
		setStyle(Style.BUBBLE);
		add(Collector.class);
	}

	public void action()
	{
		if (isFull())
		{
			returning = true;
			returnResources();
			return;
		}

		if (shouldRunHome())
		{
			returning = true;
			moveTo(getHomeBase());
			deposit();
			return;
		}

		gatherResources();
	}

	public void returnResources()
	{
		moveTo(getHomeBase());
		deposit();
	}

	public void gatherResources()
	{
		if (hasCapacity())
		{
			Resource r = getNearestResource();
			targetResource = r;
			Weapon collector = getWeaponOne();

			if (r != null && collector instanceof Collector)
			{
				moveTo(r);
				((Collector) collector).use(r);
			}
		}
	}

	private boolean shouldRunHome()
	{
		Unit enemy = getNearestEnemy();

		if (enemy == null)
		{
			return false;
		}

		if (getDistance(enemy) < 750)
		{
			return true;
		}

		return getDistance(enemy) < 1100 && getNearbyEnemyCount(1100) > getNearbyAllyCount(1200);
	}

	@Override
	public void draw(Graphics g)
	{
		super.draw(g);
		g.setColor(Color.white);
		g.setLineWidth(1);

		if (returning)
		{
			g.drawLine(getCenterX(), getCenterY(), getHomeBase().getCenterX(), getHomeBase().getCenterY());
		}
		else if (targetResource != null && !targetResource.isPickedUp())
		{
			g.drawLine(getCenterX(), getCenterY(), targetResource.getCenterX(), targetResource.getCenterY());
		}
	}
}