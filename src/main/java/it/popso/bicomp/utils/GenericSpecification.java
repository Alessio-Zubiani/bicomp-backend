package it.popso.bicomp.utils;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.jpa.domain.Specification;

import it.popso.bicomp.model.CgsLmr;
import it.popso.bicomp.model.CgsLmrLtinBulk;
import it.popso.bicomp.model.CgsLmrLtinBulkStatement;
import it.popso.bicomp.model.CgsLmrLtinBulkStatementEntry;
import it.popso.bicomp.model.Rt1Bulk;
import it.popso.bicomp.model.Rt1BulkPayment;
import it.popso.bicomp.model.Rt1PsrFileHeader;
import it.popso.bicomp.model.Rt1PsrParticipantBody;
import it.popso.bicomp.model.Rt1PsrParticipantHeader;
import it.popso.bicomp.model.Rt1PsrSettlementBic;
import it.popso.bicomp.model.TipsCamt053BankAccountStatement;
import it.popso.bicomp.model.TipsCamt053BankAccountStatementEntry;
import jakarta.persistence.criteria.Join;


public final class GenericSpecification {

    private GenericSpecification() {}
    
    
    /*
     * CGS Liquidity Transfers
     */
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasSide(Character side) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.SIDE), side);
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasAmountFrom(BigDecimal amountFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountFrom);
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasAmountTo(BigDecimal amountTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountTo);
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasSettlementDateFrom(Date settlementDateFrom) {
    	return (root, query, cb) -> {
			Join<CgsLmrLtinBulkStatementEntry, CgsLmrLtinBulkStatement> ltinBulkStatement = root.join(BicompConstants.CGS_LTIN_BULK_STATEMENT);
			
	        return cb.greaterThanOrEqualTo(ltinBulkStatement.get(BicompConstants.SETTLEMENT_DATE), settlementDateFrom);
		};
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasSettlementDateTo(Date settlementDateTo) {
    	return (root, query, cb) -> {
			Join<CgsLmrLtinBulkStatementEntry, CgsLmrLtinBulkStatement> ltinBulkStatement = root.join(BicompConstants.CGS_LTIN_BULK_STATEMENT);
			
	        return cb.lessThanOrEqualTo(ltinBulkStatement.get(BicompConstants.SETTLEMENT_DATE), settlementDateTo);
		};
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.STATUS), status);
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferHasLac(String lac) {
		return (root, query, cb) -> {
			Join<CgsLmrLtinBulkStatementEntry, CgsLmrLtinBulkStatement> ltinBulkStatement = root.join(BicompConstants.CGS_LTIN_BULK_STATEMENT);
			Join<CgsLmrLtinBulkStatement, CgsLmrLtinBulk> ltinBulk = ltinBulkStatement.join("cgsLmrLtinBulk");
			Join<CgsLmrLtinBulk, CgsLmr> ltinLmr = ltinBulk.join("cgsLmr");
			
	        return cb.equal(ltinLmr.get(BicompConstants.FILE_LAC), lac);
		};
	}
    
    public static Specification<CgsLmrLtinBulkStatementEntry> cgsLiquidityTransferSortBySettlementDateTimeAsc() {
    	return (root, query, cb) -> {
    		query.orderBy(cb.asc(root.get(BicompConstants.SETTLEMENT_DATE_TIME)));
    		
    		return cb.conjunction();
    	};
    }

    
    /*
     * RT1 Payments
     */
    public static Specification<Rt1BulkPayment> rt1PaymentHasSide(Character side) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.SIDE), side);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasAmountFrom(BigDecimal amountFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountFrom);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasAmountTo(BigDecimal amountTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountTo);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasSettlementDateFrom(Date settlementDateFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.SETTLEMENT_DATE), settlementDateFrom);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasSettlementDateTo(Date settlementDateTo) {
    	return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.SETTLEMENT_DATE), settlementDateTo);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.STATUS), status);
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasLac(String lac) {
		return (root, query, cb) -> {
			Join<Rt1BulkPayment, Rt1Bulk> bulk = root.join("rt1Bulk");
			
	        return cb.greaterThanOrEqualTo(bulk.get("fileCycle"), lac);
		};
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentHasMessageName() {
		return (root, query, cb) -> {
	        return cb.like(root.get("originalMsgName"), "pacs.".concat("%"));
		};
	}
    
    public static Specification<Rt1BulkPayment> rt1PaymentSortByAcceptanceDateTimeAsc() {
    	return (root, query, cb) -> {
    		query.orderBy(cb.asc(root.get(BicompConstants.ACCEPTANCE_DATE_TIME)));
    		
    		return cb.conjunction();
    	};
    }
    
    
    /*
     * RT1 Liquidity Transfers
     */
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasSide(Character side) {
		return (root, query, cb) -> cb.equal(root.get("operationType"), side.equals('C') ? "CPFR" : "CLRR");
	}
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasAmountFrom(BigDecimal amountFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountFrom);
	}
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasAmountTo(BigDecimal amountTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountTo);
	}
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasSettlementDateFrom(Date settlementDateFrom) {
		return (root, query, cb) -> {
			Join<Rt1PsrParticipantBody, Rt1PsrParticipantHeader> participantHeader = root.join(BicompConstants.RT1_PARTICIPANT_HEADER);
			Join<Rt1PsrParticipantHeader, Rt1PsrSettlementBic> settlementBic = participantHeader.join(BicompConstants.RT1_SETTLEMENT_BIC);
			Join<Rt1PsrSettlementBic, Rt1PsrFileHeader> fileHeader = settlementBic.join(BicompConstants.RT1_FILE_HEADER);
			
	        return cb.greaterThanOrEqualTo(fileHeader.get(BicompConstants.SETTLEMENT_DATE), settlementDateFrom);
		};
	}
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasSettlementDateTo(Date settlementDateTo) {
    	return (root, query, cb) -> {
	    	Join<Rt1PsrParticipantBody, Rt1PsrParticipantHeader> participantHeader = root.join(BicompConstants.RT1_PARTICIPANT_HEADER);
			Join<Rt1PsrParticipantHeader, Rt1PsrSettlementBic> settlementBic = participantHeader.join(BicompConstants.RT1_SETTLEMENT_BIC);
			Join<Rt1PsrSettlementBic, Rt1PsrFileHeader> fileHeader = settlementBic.join(BicompConstants.RT1_FILE_HEADER);
			
	        return cb.lessThanOrEqualTo(fileHeader.get(BicompConstants.SETTLEMENT_DATE), settlementDateTo);
    	};
	}
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferSortBySettlementDateAsc() {
    	return (root, query, cb) -> {
    		Join<Rt1PsrParticipantBody, Rt1PsrParticipantHeader> participantHeader = root.join(BicompConstants.RT1_PARTICIPANT_HEADER);
			Join<Rt1PsrParticipantHeader, Rt1PsrSettlementBic> settlementBic = participantHeader.join(BicompConstants.RT1_SETTLEMENT_BIC);
			Join<Rt1PsrSettlementBic, Rt1PsrFileHeader> fileHeader = settlementBic.join(BicompConstants.RT1_FILE_HEADER);
    		query.orderBy(cb.asc(fileHeader.get(BicompConstants.SETTLEMENT_DATE)));
    		
    		return cb.conjunction();
    	};
    }
    
    public static Specification<Rt1PsrParticipantBody> rt1LiquidityTransferHasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get("paymentStatus"), status);
	}
    
    
    /*
     * TIPS Payments
     */
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasSide(Character side) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.SIDE), side);
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasAmountFrom(BigDecimal amountFrom) {
    	return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountFrom);
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasAmountTo(BigDecimal amountTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(BicompConstants.PAYMENT_AMOUNT), amountTo);
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasSettlementDateFrom(Date settlementDateFrom) {
    	return (root, query, cb) -> {
			Join<TipsCamt053BankAccountStatementEntry, TipsCamt053BankAccountStatement> bankAccountStatement = root.join("tipsCamt053BankAccountStatement");
			
	        return cb.greaterThanOrEqualTo(bankAccountStatement.get(BicompConstants.SETTLEMENT_DATE), settlementDateFrom);
		};
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasSettlementDateTo(Date settlementDateTo) {
    	return (root, query, cb) -> {
			Join<TipsCamt053BankAccountStatementEntry, TipsCamt053BankAccountStatement> bankAccountStatement = root.join("tipsCamt053BankAccountStatement");
			
	        return cb.lessThanOrEqualTo(bankAccountStatement.get(BicompConstants.SETTLEMENT_DATE), settlementDateTo);
		};
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentHasStatus(String status) {
		return (root, query, cb) -> cb.equal(root.get(BicompConstants.STATUS), status);
	}
    
    public static Specification<TipsCamt053BankAccountStatementEntry> tipsPaymentSortBySettlementDateTimeAsc() {
    	return (root, query, cb) -> {
    		query.orderBy(cb.asc(root.get(BicompConstants.SETTLEMENT_DATE_TIME)));
    		
    		return cb.conjunction();
    	};
    }
	
}
