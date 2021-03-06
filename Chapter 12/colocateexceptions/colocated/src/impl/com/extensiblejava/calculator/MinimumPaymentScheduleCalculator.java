package com.extensiblejava.calculator;

import java.math.*;
import com.extensiblejava.loan.*;
import com.extensiblejava.loan.impl.*;

public class MinimumPaymentScheduleCalculator implements LoanCalculator {
	/* private BigDecimal presentValue;
	private BigDecimal rate;
	private int term; */
	private PaymentFactory paymentFactory;
	private BigDecimal cumulativePrincipal = new BigDecimal("0");
	private BigDecimal cumulativeInterest = new BigDecimal("0");

	public MinimumPaymentScheduleCalculator(PaymentFactory paymentFactory) {
		this.paymentFactory = paymentFactory;
	}

	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) throws CalculationException {
		try {
			PaymentSchedule paymentSchedule = this.paymentFactory.createPaymentSchedule();
			// 校正月利率
			BigDecimal adjustedRate = rate.divide(new BigDecimal("1200"), 2, BigDecimal.ROUND_UNNECESSARY);

			// 每月还款本息
			MonthlyPaymentCalculator paymentCalculator = new MonthlyPaymentCalculator();
			BigDecimal monthlyPayment = paymentCalculator.calculatePayment(presentValue, rate, term);
			
			BigDecimal loanBalance = new BigDecimal(presentValue.toString());
			while (loanBalance.doubleValue() > monthlyPayment.doubleValue()) {
				// 1. 月还款利息
				BigDecimal interest = loanBalance.multiply(adjustedRate);
				interest = interest.setScale(2, BigDecimal.ROUND_HALF_UP);
				// 2. 月还款本金
				BigDecimal principal = monthlyPayment.subtract(interest);
				principal = principal.setScale(2, BigDecimal.ROUND_HALF_UP);
				// 3. 组装Payment，并添加至还款计划PaymentSchedule
				//Payment payment = new PaymentImpl(principal, interest);
				Payment payment = this.paymentFactory.createPayment(principal, interest); 
				paymentSchedule.addPayment(payment);

				// 累积利息
				this.cumulativeInterest = this.cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
				// 累积本金
				this.cumulativePrincipal = this.cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
				// 减去已还本金
				loanBalance = loanBalance.subtract(principal);
			}

			// 最后一次还款，利息、本金、累积利息、累积本金
			BigDecimal interest = loanBalance.multiply(adjustedRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal principal = loanBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
			this.cumulativeInterest = this.cumulativeInterest.add(interest).setScale(2, BigDecimal.ROUND_HALF_UP);
			this.cumulativePrincipal = this.cumulativePrincipal.add(principal).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			// 添加最后一次的Payment
			//Payment payment = new PaymentImpl(principal, interest);
			Payment payment = this.paymentFactory.createPayment(principal, interest);
			paymentSchedule.addPayment(payment);
			
			return paymentSchedule;
		} catch (Exception e) {
			throw new CalculationException(e);
		}
		//PaymentSchedule paymentSchedule = new PaymentScheduleImpl();
		
	}

	/**
	 * 获取累积的利息
	 */
	public BigDecimal getCumulativeInterest() { return this.cumulativeInterest; }
	/**
	 * 获取累积的本金支付总额
	 */
	public BigDecimal getCumulativePrincipal() { return this.cumulativePrincipal; }
}