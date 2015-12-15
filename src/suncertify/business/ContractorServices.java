package suncertify.business;

import java.rmi.RemoteException;
import java.util.Map;

import suncertify.domain.Contractor;
import suncertify.domain.ContractorPK;

public interface ContractorServices {

	public void createBooking(Contractor contractor) throws ServicesException, RemoteException;

	public Map<Integer, Contractor> find(ContractorPK primaryKey) throws ServicesException, RemoteException;
}
