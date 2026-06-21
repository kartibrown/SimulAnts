package com.kartibrown.simulants;

public final class Position
{
	private int x;
	private int y;

	public Position(final int x, final int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object == null || getClass() != object.getClass())
			return false;

		if (this == object)
			return true;

		final Position other = (Position) object;

		return this.getX() == other.getX() && this.getY() == other.getY();
	}

	/*
	 * GETTERS & SETTERS
	 */

	public final int getX()
	{ return x; }

	public final int getY()
	{ return y; }

	public final void setX(final int x)
	{ this.x = x; }

	public final void setY(final int y)
	{ this.y = y; }
}