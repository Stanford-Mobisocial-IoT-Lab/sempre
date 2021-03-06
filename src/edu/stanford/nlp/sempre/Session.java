package edu.stanford.nlp.sempre;

import java.util.ArrayList;
import java.util.List;

/**
 * A Session contains the information specific to a user.
 * It maintains the context for discourse as well as the last example, so that
 * we can inspect the different predicted derivations and generate new training
 * examples / update parameters interactively.
 *
 * @author Percy Liang
 */
public class Session {
  public final String id;  // Session id
  private long lastAccessTime;
  public String lang; // the language we're parsing against (eg en_US or zh_TW)
  public String remoteHost;  // Where we connected from
  String format;  // html or json
  public ContextValue context;  // Current context used to create new examples
  public Example lastEx;  // Last example that we processed

  public Session(String id) {
    this.id = id;
	lastAccessTime = System.currentTimeMillis();
    context = new ContextValue(id, DateValue.now(), new ArrayList<ContextValue.Exchange>());
  }

  public Example getLastExample() { return lastEx; }
  public String getLastQuery() { return lastEx == null ? null : lastEx.utterance; }

	public long getLastAccessTime() {
		return lastAccessTime;
	}

  public void updateContext() {
	lastAccessTime = System.currentTimeMillis();
    context = context.withDate(DateValue.now());
  }

  public void updateContext(Example ex, int maxExchanges) {
    lastEx = ex;
    List<Derivation> derivations = lastEx.getPredDerivations();
    if (derivations.size() > 0) {
      Derivation deriv = derivations.get(0);
      List<ContextValue.Exchange> newExchanges = new ArrayList<>();
      newExchanges.addAll(context.exchanges);
      newExchanges.add(new ContextValue.Exchange(ex.utterance, deriv.formula, deriv.value));
      while (newExchanges.size() > maxExchanges)
        newExchanges.remove(0);
      context = context.withNewExchange(newExchanges);
    }
  }

  public void updateContextWithNewAnswer(Example ex, Derivation deriv) {
    List<ContextValue.Exchange> newExchanges = new ArrayList<>();
    for (int i = 0; i < context.exchanges.size() - 1; i++)
      newExchanges.add(context.exchanges.get(i));
    newExchanges.add(new ContextValue.Exchange(ex.utterance, deriv.formula, deriv.value));
    context = context.withNewExchange(newExchanges);
  }

  public ContextValue getContextExcludingLast() {
    List<ContextValue.Exchange> newExchanges = new ArrayList<>();
    for (int i = 0; i < context.exchanges.size() - 1; i++)
      newExchanges.add(context.exchanges.get(i));
    return context.withNewExchange(newExchanges);
  }

  @Override
  public String toString() {
    return String.format("%s: %s; last: %s", id, context, lastEx);
  }
}
