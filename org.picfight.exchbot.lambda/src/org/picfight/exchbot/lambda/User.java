
package org.picfight.exchbot.lambda;

public class User {

	public String firstName;
	public String lastName;
	public String userName;

	@Override
	public String toString () {
		return "User [firstName=" + this.firstName + ", lastName=" + this.lastName + ", userName=" + this.userName + "]";
	}

}
