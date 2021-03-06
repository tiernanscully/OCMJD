/*
 * BasicContractorService.java  1.0  16-Jan-2016
 *
 * Candidate: Tiernan Scully
 * Oracle Testing ID: OC1539331
 * Registration ID 292125773
 *
 * 1Z0-855 - Java SE 6 Developer Certified Master Assignment - English (ENU)
 */

package suncertify.business;

import suncertify.db.DBMainExtended;
import suncertify.db.RecordNotFoundException;
import suncertify.domain.Contractor;
import suncertify.domain.ContractorPk;
import suncertify.util.ContractorConverter;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * BasicContractorService is the default implementation of {@link ContractorService}. It has an
 * implementation for booking and finding contractors which satisfy certain search criteria from a
 * specified data access object.
 */
public class BasicContractorService implements ContractorService {

  /**
   * The default message prefix for {@link ContractorNotFoundException} messages.
   */
  private static final String MESSAGE_PREFIX = "Could not find any contractors with ";

  /** The data access object used to interact with the database. */
  private final DBMainExtended data;

  /**
   * Constructs a new BasicContractorService with the specified {@code data}.
   *
   * @param data
   *          the data access object used to interact with the database.
   * @throws IllegalArgumentException
   *           if {@code data} is null.
   */
  public BasicContractorService(final DBMainExtended data) {
    if (data == null) {
      throw new IllegalArgumentException("Database object cannot be null.");
    }
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void book(final Contractor contractor) throws ContractorNotFoundException,
      AlreadyBookedException, RemoteException, IllegalArgumentException {
    if (contractor == null) {
      throw new IllegalArgumentException("Contractor cannot be null.");
    }
    int recordNumber = -1;
    try {
      final String[] fieldValues = contractor.toStringArray();
      final String[] uniqueId = contractor.getPrimaryKey().toStringArray();
      recordNumber = data.find(uniqueId)[0];
      data.lock(recordNumber);
      checkContractorIsAvailable(recordNumber);
      data.update(recordNumber, fieldValues);
    } catch (final RecordNotFoundException e) {
      throw new ContractorNotFoundException(MESSAGE_PREFIX + contractor.getPrimaryKey(), e);
    } finally {
      data.unlock(recordNumber);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<Integer, Contractor> find(final ContractorPk searchKey)
      throws ContractorNotFoundException, RemoteException, IllegalArgumentException {
    if (searchKey == null) {
      throw new IllegalArgumentException("ContractorPk cannot be null.");
    }
    final Map<Integer, Contractor> matchingRecords = new HashMap<>();
    try {
      final String[] searchCriteria = searchKey.toStringArray();
      final int[] recordNumbers = data.find(searchCriteria);
      for (final int recordNumber : recordNumbers) {
        final String[] fieldValues = data.read(recordNumber);
        final Contractor contractor = ContractorConverter.toContractor(fieldValues);
        final ContractorPk primaryKey = contractor.getPrimaryKey();
        if (doesMatchExactly(primaryKey, searchKey)) {
          matchingRecords.put(recordNumber, contractor);
        }
      }

      if (matchingRecords.isEmpty()) {
        throw new ContractorNotFoundException(MESSAGE_PREFIX + searchKey);
      }

    } catch (final RecordNotFoundException e) {
      throw new ContractorNotFoundException(MESSAGE_PREFIX + searchKey, e);
    }
    return matchingRecords;
  }

  /**
   * Check that the contractor with the specified record number is available for booking. Throws an
   * {@link AlreadyBookedException} if the contractor is booked.
   *
   * @param recordNumber
   *          the number of the contractor record.
   * @throws AlreadyBookedException
   *           if the contractor with the specified record number has already been booked.
   * @throws RemoteException
   *           if an RMI communication-related exception occurs.
   * @throws RecordNotFoundException
   *           if the record does not exist or has been marked as deleted in the database.
   */
  private void checkContractorIsAvailable(final int recordNumber)
      throws RecordNotFoundException, AlreadyBookedException, RemoteException {
    final String[] fieldValues = data.read(recordNumber);
    final Contractor contractor = ContractorConverter.toContractor(fieldValues);
    if (contractor.isBooked()) {
      throw new AlreadyBookedException(
          "Contractor with " + contractor.getPrimaryKey() + " already has an existing booking.");
    }
  }

  /**
   * Compares the two specified {@link ContractorPk} arguments and returns true if their fields,
   * {@code name} and {@code location}, are equal. Note: It will only compare fields that are not
   * empty in the {@code searchKey}. <br>
   * <br>
   * <b>Example 1:</b> If a {@code searchKey} with {@code name}="Fred" and {@code location}="Paris"
   * is specified, this method will only return true if the {@code name} and {@code location} fields
   * of the specified {@code primaryKey} is equal to "Fred" and "Paris" respectively.<br>
   * <br>
   * <b>Example 2:</b> If a {@code searchKey} with {@code name}="Fred" and {@code location}="" is
   * specified, this method will only return true if the {@code name} field of the specified
   * {@code primaryKey} is equal to "Fred".<br>
   * <br>
   * <b>Example 3:</b> If a {@code searchKey} with {@code name}="" and {@code location}="Paris" is
   * specified, this method will only return true if the {@code location} field of the specified
   * {@code primaryKey} is equal to "Paris".<br>
   * <br>
   * <b>Example 4:</b> If a {@code searchKey} with {@code name}="" and {@code location}="" is
   * specified, this method will return true.<br>
   *
   * @return true, if the non-blank fields of the specified {@code searchKey} match the
   *         corresponding fields of the specified {@code primaryKey}.
   */
  private boolean doesMatchExactly(final ContractorPk primaryKey, final ContractorPk searchKey) {
    boolean isMatch = true;
    final String[] fieldValues = primaryKey.toStringArray();
    final String[] searchValues = searchKey.toStringArray();
    for (int index = 0; index < searchValues.length; index++) {
      final String searchValue = searchValues[index];

      if (searchValue.isEmpty()) {
        break;
      }
      if (!searchValue.equals(fieldValues[index])) {
        isMatch = false;
        break;
      }
    }
    return isMatch;
  }
}
