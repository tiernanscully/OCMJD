package suncertify.business.rmi;

import static suncertify.util.Constants.RMI_ID;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import suncertify.business.BasicContractorService;
import suncertify.db.DBMainExtended;

/**
 * Subclass of {@link BasicContractorService} and implements of
 * {@link RMIService}. Contains the business logic necessary to start an RMI
 * server instance on a specified port number and implement the business methods
 * defined the ContractorService interface
 */
public class RMIServer extends BasicContractorService implements RMIService {

	/**
	 * Constructs a new RMI server instance with the specified database manager.
	 *
	 * @param databaseManager
	 *            the database manager used to interact with the database.
	 * @throws RemoteException
	 *             if an RMI communication-related exception occurs.
	 */
	public RMIServer(final DBMainExtended data) throws RemoteException {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startServer(final int port) throws RemoteException {
		final RMIService impl = (RMIService) UnicastRemoteObject.exportObject(this, 0);
		final Registry registry = LocateRegistry.createRegistry(port);
		registry.rebind(RMI_ID, impl);
	}
}
