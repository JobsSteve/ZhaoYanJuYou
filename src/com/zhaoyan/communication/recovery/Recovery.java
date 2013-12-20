package com.zhaoyan.communication.recovery;

abstract public class Recovery {
	protected abstract boolean doRecovery();
	public abstract void getLastSatus();
	/**
	 * Attempt to recovery current communication.
	 * 
	 * @return If recovery is going to be done, return true. If recovery will
	 *         not be done, return false.
	 */
	public boolean attemptRecovery() {
		if (!checkRecoveryNecessityBase()) {
			return false;
		}

		return doRecovery();
	}

	private boolean checkRecoveryNecessityBase() {
		if (checkIgnoreConditions()) {
			return false;
		}
		return checkRecoveryNecessity();
	}

	/**
	 * Check whether to recovery or not.
	 * 
	 * @return If recovery should be done, return true . If recovery should not
	 *         be done, return false.
	 */
	protected boolean checkRecoveryNecessity() {
		return true;
	}

	/**
	 * Check whether recovery should be ignore.
	 * 
	 * @return If recovery should be ignored, return true . If recovery should
	 *         not be ignored, return false.
	 */
	protected boolean checkIgnoreConditions() {

		return false;
	}
}
