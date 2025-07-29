package it.popso.bicomp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import it.popso.bicomp.dto.FeedbackDto;
import it.popso.bicomp.dto.FeedbackEventiDto;
import it.popso.bicomp.exception.ResourceNotFoundException;
import it.popso.bicomp.model.Feedback;
import it.popso.bicomp.model.FeedbackEventi;
import it.popso.bicomp.repository.FeedbackEventiRepository;
import it.popso.bicomp.repository.FeedbackRepository;
import it.popso.bicomp.service.impl.FeedbackServiceImpl;
import it.popso.bicomp.utils.FeedbackStatus;


@ExtendWith(SpringExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(OrderAnnotation.class)
class FeedbackServiceImplTest {
	
	@Mock
	private FeedbackRepository feedbackRepository;
	
	@Mock
	private FeedbackEventiRepository feedbackEventiRepository;
	
	private ModelMapper modelMapper;
	private FeedbackServiceImpl service;
	
	
	@BeforeEach
    public void setup() {
		this.modelMapper = new ModelMapper();
		this.service = new FeedbackServiceImpl(this.feedbackRepository, this.feedbackEventiRepository, this.modelMapper);
	}
	
	@Test
	@Order(1)
	void testFindAllFeedbacks() {
		
		Feedback f1 = Feedback.builder()
				.id(new BigDecimal(1))
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.updateUser(new BigDecimal(1))
				.stato(FeedbackStatus.INSERITO.name())
				.tmsInsert(new Date())
				.tmsUpdate(new Date())
				.build();
		Feedback f2 = Feedback.builder()
				.id(new BigDecimal(2))
				.descrizione("Feedback 2")
				.insertUser(new BigDecimal(2))
				.updateUser(new BigDecimal(1))
				.stato(FeedbackStatus.IN_LAVORAZIONE.name())
				.tmsInsert(new Date())
				.tmsUpdate(new Date())
				.build();
		Feedback f3 = Feedback.builder()
				.id(new BigDecimal(3))
				.descrizione("Feedback 3")
				.insertUser(new BigDecimal(3))
				.updateUser(new BigDecimal(1))
				.stato(FeedbackStatus.RISOLTO.name())
				.tmsInsert(new Date())
				.tmsUpdate(new Date())
				.build();
		List<Feedback> expectedFeedbacks = Arrays.asList(f1, f2, f3);
		Mockito.when(this.feedbackRepository.findAllByOrderByTmsInsertDesc()).thenReturn(expectedFeedbacks);
		
		List<FeedbackDto> feedbacks = this.service.findAllFeedback();
		
		verify(this.feedbackRepository, times(1)).findAllByOrderByTmsInsertDesc();
		assertThat(feedbacks).hasSize(3);
		assertThat(feedbacks.get(0).getStato()).isEqualTo(f1.getStato());
		assertThat(feedbacks.get(1).getUpdateUser()).isEqualTo(f2.getUpdateUser());
		assertThat(feedbacks.get(2).getTmsInsert()).isEqualTo(f3.getTmsInsert());
	}
	
	@Test
	@Order(2)
	void testFindAllFeedbacksNotFound() {
		
		List<Feedback> expectedFeedbacks = Arrays.asList();
		Mockito.when(this.feedbackRepository.findAllByOrderByTmsInsertDesc()).thenReturn(expectedFeedbacks);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findAllFeedback();
		});
		
		verify(this.feedbackRepository, times(1)).findAllByOrderByTmsInsertDesc();
	}
	
	@Test
	@Order(3)
	void testFindById() {
		
		Optional<Feedback> o = Optional.of(Feedback.builder()
				.id(new BigDecimal(1))
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.updateUser(new BigDecimal(1))
				.stato(FeedbackStatus.INSERITO.name())
				.tmsInsert(new Date())
				.tmsUpdate(new Date())
				.build()
			);
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		FeedbackDto result = this.service.findById(BigDecimal.ONE);
		
		assertThat(result.getId()).isEqualTo(o.get().getId());
		assertThat(result.getInsertUser()).isEqualTo(o.get().getInsertUser());
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(4)
	void testFindByIdNotFound() {
		
		Optional<Feedback> o = Optional.empty();
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.findById(BigDecimal.ONE);
		});
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(5)
	void testCreateFeedback() {
		
		Feedback f = Feedback.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		FeedbackEventi fe = FeedbackEventi.builder()
				.descrizione(f.getDescrizione())
				.insertUser(f.getInsertUser())
				.stato(FeedbackStatus.INSERITO.name())
				.feedback(f)
				.build();
		
		FeedbackDto dto = FeedbackDto.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenReturn(f);
		Mockito.when(this.feedbackEventiRepository.save(Mockito.any(FeedbackEventi.class))).thenReturn(fe);
		
		FeedbackDto result = this.service.createFeedback(dto);
		
		assertThat(result).isNotNull();
		assertThat(result.getStato()).isEqualTo("INSERITO");
		
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(1)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(6)
	void testCreateFeedbackDataIntegrityViolationException() {
		
		FeedbackDto dto = FeedbackDto.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenThrow(
				new DataIntegrityViolationException("DataIntegrityViolationException")
		);

		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.createFeedback(dto);
		});
		
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(0)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(7)
	void testCreateFeedbackEventiDataIntegrityViolationException() {
		
		Feedback f = Feedback.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		
		FeedbackDto dto = FeedbackDto.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenReturn(f);
		Mockito.when(this.feedbackEventiRepository.save(Mockito.any(FeedbackEventi.class))).thenThrow(
				new DataIntegrityViolationException("DataIntegrityViolationException")
		);

		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.createFeedback(dto);
		});
		
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(1)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(8)
	void testFindEventsByFeedbackId() {
		
		Feedback f = Feedback.builder()
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		List<FeedbackEventi> list = Arrays.asList(FeedbackEventi.builder()
				.id(new BigDecimal(1))
				.descrizione(f.getDescrizione())
				.insertUser(f.getInsertUser())
				.stato(FeedbackStatus.INSERITO.name())
				.feedback(f)
				.build(), FeedbackEventi.builder()
				.id(new BigDecimal(2))
				.descrizione(f.getDescrizione())
				.insertUser(f.getInsertUser())
				.stato(FeedbackStatus.IN_LAVORAZIONE.name())
				.feedback(f)
				.build()
		);
		Mockito.when(this.feedbackEventiRepository.findByFeedbackIdOrderByTmsInsertDesc(Mockito.any(BigDecimal.class)))
			.thenReturn(list);
		
		List<FeedbackEventiDto> result = this.service.findEventsByFeedbackId(new BigDecimal(1));
		
		assertThat(result).isNotNull().hasSameSizeAs(list);
		
		verify(this.feedbackEventiRepository, times(1)).findByFeedbackIdOrderByTmsInsertDesc(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(9)
	void testFindEventsByFeedbackIdEmpty() {
		
		List<FeedbackEventi> list = Arrays.asList();
		Mockito.when(this.feedbackEventiRepository.findByFeedbackIdOrderByTmsInsertDesc(Mockito.any(BigDecimal.class)))
			.thenReturn(list);
		
		List<FeedbackEventiDto> result = this.service.findEventsByFeedbackId(new BigDecimal(1));
		
		assertThat(result).isNotNull().isEmpty();
		
		verify(this.feedbackEventiRepository, times(1)).findByFeedbackIdOrderByTmsInsertDesc(Mockito.any(BigDecimal.class));
	}
	
	@Test
	@Order(10)
	void testUpdateFeedback() {
		
		Optional<Feedback> o = Optional.of(Feedback.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build()
			);
		FeedbackEventi fe = FeedbackEventi.builder()
				.id(BigDecimal.ONE)
				.descrizione(o.get().getDescrizione())
				.insertUser(o.get().getInsertUser())
				.stato(FeedbackStatus.INSERITO.name())
				.feedback(o.get())
				.build();
		
		FeedbackDto dto = FeedbackDto.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenReturn(o.get());
		Mockito.when(this.feedbackEventiRepository.save(Mockito.any(FeedbackEventi.class))).thenReturn(fe);
		
		this.service.updateFeedback(BigDecimal.ONE, dto);
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(1)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(11)
	void testUpdateFeedbackNotFound() {
		
		Optional<Feedback> o = Optional.empty();
		FeedbackDto dto = FeedbackDto.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		
		assertThrows(ResourceNotFoundException.class, () -> {
			this.service.updateFeedback(BigDecimal.ONE, dto);
		});
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.feedbackRepository, times(0)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(0)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(12)
	void testUpdateFeedbackDataIntegrityViolationException() {
		
		Optional<Feedback> o = Optional.of(Feedback.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build()
			);
		
		FeedbackDto dto = FeedbackDto.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenThrow(
				new DataIntegrityViolationException("DataIntegrityViolationException")
		);
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.updateFeedback(BigDecimal.ONE, dto);
		});
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(0)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(13)
	void testUpdateFeedbackEventiDataIntegrityViolationException() {
		
		Optional<Feedback> o = Optional.of(Feedback.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build()
			);
		
		FeedbackDto dto = FeedbackDto.builder()
				.id(BigDecimal.ONE)
				.descrizione("Feedback 1")
				.insertUser(new BigDecimal(1))
				.build();
		Mockito.when(this.feedbackRepository.findById(Mockito.any(BigDecimal.class))).thenReturn(o);
		Mockito.when(this.feedbackRepository.save(Mockito.any(Feedback.class))).thenReturn(o.get());
		Mockito.when(this.feedbackEventiRepository.save(Mockito.any(FeedbackEventi.class))).thenThrow(
				new DataIntegrityViolationException("DataIntegrityViolationException")
		);
		
		assertThrows(DataIntegrityViolationException.class, () -> {
			this.service.updateFeedback(BigDecimal.ONE, dto);
		});
		
		verify(this.feedbackRepository, times(1)).findById(Mockito.any(BigDecimal.class));
		verify(this.feedbackRepository, times(1)).save(Mockito.any(Feedback.class));
		verify(this.feedbackEventiRepository, times(1)).save(Mockito.any(FeedbackEventi.class));
	}
	
	@Test
	@Order(14)
	void testFeedbackServiceImplEquals() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(this.feedbackRepository, this.feedbackEventiRepository, 
				this.modelMapper);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(15)
	void testFeedbackServiceImplEqualsSameInstance() {
		
		assertThat(this.service.equals(this.service)).isTrue();
	}
	
	@Test
	@Order(16)
	void testFeedbackServiceImplNotEqualsNotSameInstanceType() {
		
		assertThat(this.service.equals("test")).isFalse();
	}
	
	@Test
	@Order(17)
	void testFeedbackServiceImplEqualsNull() {
		
		this.service = new FeedbackServiceImpl(null, null, null);
		FeedbackServiceImpl test = new FeedbackServiceImpl(null, null, null);
		boolean result = this.service.equals(test);
		assertThat(result).isTrue();
	}
	
	@Test
	@Order(18)
	void testFeedbackServiceImplNotNull() {
		
		this.service = new FeedbackServiceImpl(null, null, null);
		FeedbackServiceImpl test = new FeedbackServiceImpl(this.feedbackRepository, this.feedbackEventiRepository, 
				this.modelMapper);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(19)
	void testFeedbackServiceImplNotEqualsFeedbackNull() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(null, this.feedbackEventiRepository, this.modelMapper);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(20)
	void testFeedbackServiceImplNotEqualsStanNull() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(this.feedbackRepository, null, this.modelMapper);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(21)
	void testFeedbackServiceImplNotEqualsProcNull() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(this.feedbackRepository, this.feedbackEventiRepository, null);
		boolean result = this.service.equals(test);
		assertThat(result).isFalse();
	}
	
	@Test
	@Order(22)
	void testFeedbackServiceImplEqualsHashCode() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(this.feedbackRepository, this.feedbackEventiRepository, 
				this.modelMapper);
		int result = this.service.hashCode();
		assertThat(result).isEqualTo(test.hashCode());
	}
	
	@Test
	@Order(23)
	void testFeedbackServiceImplNotEqualsHashCode() {
		
		FeedbackServiceImpl test = new FeedbackServiceImpl(null, null, null);
		int result = this.service.hashCode();
		assertThat(result).isNotEqualTo(test.hashCode());
	}

}
