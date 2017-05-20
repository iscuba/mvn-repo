package lex;


import errors.LexicalError;

import symbolTable.KeywordTable;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableEntry;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;

/*
 * TODO: Assignment #1
 */
public class Tokenizer
{
	private CharStream stream = null;
	// create a buffer to hold read chars
	private CharBuffer buff;
	// create Token for last token returned
	private Token lastToken = null;
	//Length of the longest possible identifier
	private int MAX_ID_LENGTH;
	private Token t = new Token();
	private KeywordTable keywordTable;
	private SymbolTable table;


    int testCnt = 0;



	public Tokenizer(String filename) throws IOException, LexicalError
	{
		super();
		init(new CharStream(filename));
	}

	/** Used during testing to read files from the classpath. */
	public Tokenizer(URL url) throws IOException, LexicalError
	{
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError
	{
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream)
	{
		this.stream = stream;
		MAX_ID_LENGTH = 64;
		keywordTable = new KeywordTable();
		setLastToken(new Token());
		// TODO more initialization will be needed...
	}

	public int getLineNumber()
	{
		return stream.lineNumber();
	}

	public Token getLastToken(){
		return lastToken;
	}

	public void setLastToken(Token t){
		this.lastToken = t;
	}
	//___________________
	//     getNextToken
	//____________________
	// Returns the next token

	public Token getNextToken() throws LexicalError
	{
		t = new Token();

		while (true) {
			char seen = stream.currentChar();
			//if we see a newline op or blank space, go to the next char
			if ( seen == '\n' || seen == ' '){
				seen = stream.currentChar();
			}
			if (Character.isDigit(seen)) {
				t = readDigit(seen);

			} else if (Character.isLetter(seen)){
				t = readLetter(seen);

			} else if (isMulOp(seen)){
				t = readMulOp(seen);

			} else if (isAddOp(seen)) {
				t = readAddOp(seen);

			} else if (isRelOpOrAssign(seen)){
				t = readRelOp(seen);

			} else {
				readOther(seen);
			}

            lastToken = new Token();
            lastToken.setType(t.getType());
            lastToken.setValue(t.getValue());
            return t;
		}

	}

	/** todo **/
	//---------------------
    //readDigit(Character seen) --> Token
    //      returns the appropriate token (either intconstant or realconstant) based
    //      on the characters seen
    //---------------------
	private Token readDigit(Character seen) throws LexicalError {
        StringBuffer numbers = new StringBuffer();
        while (Character.isDigit(seen)) {
            numbers.append(Character.toString(seen));
            seen = stream.currentChar();
        }
        if (seen == '.') {
            char lookahead = stream.currentChar();
            if (Character.isDigit(lookahead)) {
                numbers.append(Character.toString(seen));
                do {
                    numbers.append(Character.toString(lookahead));
                    lookahead = stream.currentChar();
                } while (Character.isDigit(lookahead));
                // we might have exponents:
                char lookForExp = stream.currentChar();
                //if we see an E in the stream,
                if (Character.toString(lookForExp).toUpperCase().equals("E")) {
                    if (lookForExp == '+' || lookForExp == '-' || Character.isDigit(lookForExp)) {
                        char lookahead3 = stream.currentChar();
                        if (Character.isDigit(lookahead3)) {
                            // we have a real number exponent, so we should add the exponant digits.
                            numbers.append(Character.toString(lookahead));
                            numbers.append(Character.toString(lookForExp));
                            numbers.append(Character.toString(lookahead3));
                            lookahead3 = stream.currentChar();
                            while (Character.isDigit(lookahead3)) {
                                numbers.append(Character.toString(lookahead3));
                                lookahead3 = stream.currentChar();
                            }
                            stream.pushBack(lookahead3);
                            t.setType(TokenType.REALCONSTANT);
                            t.setValue(numbers.toString());
                        } else {
                            // it is not a digit and we push back the non-digit we looked at
                            stream.pushBack(lookahead3);
                            stream.pushBack(lookForExp);
                            stream.pushBack(lookahead);
                            t.setType(TokenType.REALCONSTANT);
                            t.setValue(numbers.toString());
                        }
                    } else {
                        // The lookahead id not  +,-, or DIGIT so the E must be part of something else.
                        //Push back the lookaheads
                        stream.pushBack(lookForExp);
                        stream.pushBack(lookahead);
                        t.setType(TokenType.REALCONSTANT);
                        t.setValue(numbers.toString());
                    }
                } else {
                    // what we see must not be E or DIGIT, so push it back
                    stream.pushBack(lookForExp);
                    stream.pushBack(lookahead);
                    // make the token a REAL
                    t.setType(TokenType.REALCONSTANT);
                    t.setValue(numbers.toString());
                }

            } else {
                // The . is not followed by a numbersber, it is a part of something else.
                // push back the lookahead and make the token an integer
                stream.pushBack(lookahead);
                stream.pushBack(seen);
                t.setType(TokenType.INTCONSTANT);
                t.setValue(numbers.toString());
            }

        }
        // we have not seen a period (.)
        else {
            //Check for an E for exponants.
                //if there is an exponent, then it's a real
                // 2a. MAYBE EXPONENT
            if (((Character.toString(seen)).toUpperCase().equals("E"))) {
                // when we see the E, we need a lookahead to gether the rest of the digits
                char lookahead = stream.currentChar();
                //if we see +, - or Digit
                if (lookahead == '+' || lookahead == '-' || Character.isDigit(lookahead)) {
                    //check if the number is an exponent
                    char lookForExp = stream.currentChar();
                    if (Character.isDigit(lookForExp)) {
                        //found a number with an exponent
                        numbers.append(Character.toString(seen));
                        numbers.append(Character.toString(lookahead));
                        numbers.append(Character.toString(lookForExp));
                        lookForExp = stream.currentChar();
                        while (Character.isDigit(lookForExp)) {
                            numbers.append(Character.toString(lookForExp));
                            lookForExp = stream.currentChar();
                        }
                        stream.pushBack(lookForExp);
                        t.setType(TokenType.REALCONSTANT);
                        t.setValue(numbers.toString());
                    } else {
                        stream.pushBack(lookForExp);
                        stream.pushBack(lookahead);
                        stream.pushBack(seen);
                        t.setType(TokenType.INTCONSTANT);
                        t.setValue(numbers.toString());
                    }
                } else {
                    // the next character is not + - or DIGIT, so the E must be the start of something else.
                    stream.pushBack(lookahead);
                    stream.pushBack(seen);
                    t.setType(TokenType.INTCONSTANT);
                    t.setValue(numbers.toString());
                }
            } else {
                //There is no Exponant so push the lookaheads back and make it a constant
                stream.pushBack(seen);
                t.setType(TokenType.INTCONSTANT);
                t.setValue(numbers.toString());
            }
        }
        return t;
    }




	//------------------------
    //readLetter(Character seen) --> Token
    //   Makes a StringBuffer of all the letters found next to each other, and calls a sister
    //   Function which identifies the proper Token Type for the string found and sets the
    //   Token type accordingly;
    //-------------------------

	private Token readLetter(Character seen) throws LexicalError{
        StringBuffer name = new StringBuffer().append(seen);
        seen = stream.currentChar();
        while (Character.isLetter(seen)) {
            if (MAX_ID_LENGTH <= name.length()) {
                throw LexicalError.IdentifierTooLong(name.toString());
            } else {
                name.append(seen);
                seen = stream.currentChar();
            }
        }
        stream.pushBack(seen);

        /*System.out.print("READLETTER TESTING");
        System.out.println();
        System.out.print(name.toString());
        System.out.println();*/

        return whichTokenType(name.toString());
	}

    //whichTokenType(String str) ---> Token
	//--------------------------------------
    //    Takes a string and determines whether it is an identifier or
    //    keyword, and sets the token type accordingly.
	private Token whichTokenType(String str) {
        String upperID = str.toUpperCase();
        SymbolTableEntry s = keywordTable.lookup(upperID);
        if (s != null) {
            switch(upperID) {
                //add all the op types here. Key Words will already have been inserted through
                // Symbol table innitialization.
                case "AND":
                    t.setType(s.getType());
                    t.setOpType(Token.OperatorType.AND);
                    break;
                case "OR":
                    t.setType(s.getType());
                    t.setOpType(Token.OperatorType.OR);
                    break;
                case "NOT":
                    t.setType(s.getType());
                    // System.out.println("MADE IT TO NOT" + testCnt);
                    // testCnt++;
                    t.setOpType(Token.OperatorType.NOT);
                    break;
                case "DIV":
                    t.setType(s.getType());
                    t.setOpType(Token.OperatorType.INTEGERDIVIDE);
                    break;
                case "MOD":
                    t.setType(s.getType());
                    t.setOpType(Token.OperatorType.MOD);
                    break;
                    //this is for it its anything other than ^
                //might have to fix this when I add my KeywordTable
                // Whay is this running forever??
                case "PROGRAM":
                    t.setType(s.getType());
                    t.setType(TokenType.PROGRAM);
                    break;

                case "END":
                    t.setType(TokenType.END);
                    break;

                default:
                    // System.out.println("MADE IT TO DEFAULT " + str);
                    //should never reach here...
                    t.setType(s.getType());
                    break;
            }

        } else {
             //make the identifier (it's not a keyword)
            t.setType(TokenType.IDENTIFIER);
            t.setValue(str);
        }
        return t;

    }

    private boolean isMulop(Character ch) {
	    return (ch == '*' || ch == '/');
    }

	private Token readMulOp(char seen) throws LexicalError {
        switch (seen) {
            case '*':
                t.setType(TokenType.MULOP);
                t.setOpType(Token.OperatorType.MULTIPLY);
                break;
            case '/' :
                t.setType(TokenType.MULOP);
                t.setOpType(Token.OperatorType.DIVIDE);
                break;

        }
        return t;
	}

	private boolean isAddOp(char ch) {
	    return ( ch == '+' || ch == '-');
    }

	private Token readAddOp(Character seen) throws LexicalError{
        TokenType lastTokenType = getLastToken().getType();
        if (lastTokenType == TokenType.RIGHTPAREN ||
                lastTokenType == TokenType.RIGHTBRACKET ||
                lastTokenType == TokenType.IDENTIFIER ||
                lastTokenType == TokenType.INTCONSTANT ||
                lastTokenType == TokenType.REALCONSTANT) {
            if (seen == '+') {
                t.setType(TokenType.ADDOP);
                t.setOpType(Token.OperatorType.ADD);
            } else if (seen == '-') {
                t.setType(TokenType.ADDOP);
                t.setOpType(Token.OperatorType.SUBTRACT);
            } else {
                //Throw error
                throw LexicalError.IllegalCharacter(seen, getLineNumber());

            }
        } else {

            if (seen == '-') {
                t.setType(TokenType.UNARYMINUS);
            }
            else if (seen == '+') {
                t.setType(TokenType.UNARYPLUS);

            } else {
                //Do I throw an error??

            }
        }
        return t;
	}

	private boolean isRelopOrAssign(char ch) {
        return ch == '=' || ch == '>' || ch == '<';
    }

	private Token readRelOp(Character seen) throws LexicalError{
	    char nextChar;
	    switch (seen) {
            case '=':
                t.setOpType(Token.OperatorType.EQUAL);
                t.setType(TokenType.RELOP);
                break;
            case '>':
                nextChar = stream.currentChar();
                if (nextChar == '=') {
                    t.setOpType(Token.OperatorType.GREATERTHANOREQUAL);
                    t.setType(TokenType.RELOP);
                } else {
                    t.setOpType(Token.OperatorType.GREATERTHAN);
                    t.setType(TokenType.RELOP);
                    stream.pushBack(nextChar);
                }
                break;

            case '<':
                nextChar = stream.currentChar();
                if (nextChar == '=') {
                    t.setOpType(Token.OperatorType.LESSTHANOREQUAL);
                    t.setType(TokenType.RELOP);
                } else if (nextChar == '>') {
                    t.setOpType(Token.OperatorType.NOTEQUAL);
                    t.setType(TokenType.RELOP);
                } else {
                    t.setOpType(Token.OperatorType.LESSTHAN);
                    t.setType(TokenType.RELOP);
                    stream.pushBack(nextChar);
                }
                break;


        }
        return t;
	}

	private Token readOther(Character seen) throws LexicalError {
        switch (seen) {
            case '(':
                t.setType(TokenType.LEFTPAREN);
                break;
            case ')':
                t.setType(TokenType.RIGHTPAREN);
                break;
            case '[':
                t.setType(TokenType.LEFTBRACKET);
                break;
            case ']':
                t.setType(TokenType.RIGHTBRACKET);
                break;
            case ';':
                t.setType(TokenType.SEMICOLON);
                break;
            case ':':
                char s = stream.currentChar();
                if (s == '=') {
                    t.setType(TokenType.ASSIGNOP);
                } else {
                    t.setType(TokenType.COLON);
                    stream.pushBack(s);
                }
                break;
            case ',':
                t.setType(TokenType.COMMA);
                break;
            case '.':
                char lookahead = stream.currentChar();
                // if the next thing is also a dot, then we got doubledot
                if (lookahead == '.') {
                    t.setType(TokenType.DOUBLEDOT);
                } else {
                    // if the next thing isn't a dot, then we have an end marker
                    stream.pushBack(lookahead);
                    t.setType(TokenType.ENDMARKER);
                }
                break;
            case CharStream.EOF:
                t.setType(TokenType.ENDOFFILE);
                break;
            default:
                // If it isn't valid... throw an error
                if (!stream.valid(seen)) {
                    throw LexicalError.IllegalCharacter(seen, stream.lineNumber());
                }
        }
        return t;


	}

	//------------------------
	//     Identifiers
	//------------------------

	private Boolean isMulOp(Character seen) {
        return seen == '*' || seen == '/';
	}

	private Boolean isAddOp(Character seen) {
        return seen == '+' || seen == '-';
	}

	// Returns T if we saw the beginning of a Relational Operator
	private Boolean isRelOpOrAssign(Character seen) {
		return seen == '=' || seen == '<' || seen == '>';
	}

	// TODO Much (much) more code goes here...
}
