import pygame, sys, time

playerHeight = 70
playerWidth = 10
playerXOffset = 20
playerVelocity = 8
ballRadius = 10
ballVelocityX = 10
ballVelocityY = 11
lowerXBound = 0
upperXBound = 1300
lowerYBound = 0
upperYBound = 600
fps = 60

class player():
    def __init__(self, posX, posY, height, width, lowerXBound = lowerXBound, upperXBound = upperXBound, lowerYBound = lowerYBound, upperYBound = upperYBound):
        self.posX = posX
        self.posY = posY
        self.height = height
        self.width = width
        self.lowerXBound = lowerXBound
        self.upperXBound = upperXBound
        self.lowerYBound = lowerYBound
        self.upperYBound = upperYBound
        self.rect = pygame.Rect(posX - width, posY - height, width*2, height*2)

    def getYBounds(self):
        return (self.posY - self.height, self.posY + self.height)

    def getXBounds(self):
        return (self.posX - self.width, self.posX + self.width)

    def moveUp(self, dist):
        prevY = self.posY
        self.posY -= dist
        (y1, y2) = self.getYBounds()
        if y1 < self.lowerYBound:
            self.posY = self.lowerYBound + (self.lowerYBound - y1) + self.height
        self.rect.move_ip(0, self.posY - prevY)

    def moveDown(self, dist):
        prevY = self.posY
        self.posY += dist
        (y1, y2) = self.getYBounds()
        if y2 > self.upperYBound:
            self.posY = self.upperYBound - (y2 - self.upperYBound) - self.height
        self.rect.move_ip(0, self.posY - prevY)

    def checkCollision(self, ball):
        (x1, x2) = self.getXBounds()
        (y1, y2) = self.getYBounds()
        if ball.posX + ball.ballRadius >= x1 and ball.posX - ball.ballRadius <= x2 and ball.posY + ball.ballRadius >= y1 and ball.posY - ball.ballRadius <= y2:
            return True
        return False

class ball():
    def __init__(self, posX, posY, ballRadius, velocityX, velocityY, lowerXBound, upperXBound, lowerYBound, upperYBound, leftPlayer, rightPlayer):
        self.posX = posX
        self.posY = posY
        self.ballRadius = ballRadius
        self.velocityX = velocityX
        self.velocityY = velocityY
        self.lowerXBound = lowerXBound
        self.upperXBound = upperXBound
        self.lowerYBound = lowerYBound
        self.upperYBound = upperYBound
        self.leftPlayer = leftPlayer
        self.rightPlayer = rightPlayer

    def move(self):
        self.posY += self.velocityY
        if self.posY > self.upperYBound:
            self.velocityY *= -1
            self.posY = self.upperYBound - (self.upperYBound - self.posY)
        if self.posY < self.lowerYBound:
            self.velocityY *= -1
            self.posY = self.lowerYBound + (self.lowerYBound - self.posY)
        self.posX += self.velocityX
        if self.velocityX > 0:
            if self.rightPlayer.checkCollision(self):
                self.velocityX *= -1
                self.posX = self.rightPlayer.getXBounds()[0] - (self.rightPlayer.getXBounds()[0] - self.posX) # - self.ballRadius*2
        else:
            if self.leftPlayer.checkCollision(self):
                self.velocityX *= -1
                self.posX = self.leftPlayer.getXBounds()[1] + (self.leftPlayer.getXBounds()[1] -self.posX) + self.ballRadius*2
        if self.posX > self.upperXBound:
            print(ballVelocityX)
            return -1
        if self.posX < self.lowerXBound:
            print(ballVelocityX)
            return 1
        return 0

def createPlayers():
    left = player(lowerXBound + playerXOffset, lowerYBound + (upperYBound-lowerYBound)/2, playerHeight, playerWidth)
    right = player(upperXBound - playerXOffset, lowerYBound + (upperYBound-lowerYBound)/2, playerHeight, playerWidth)
    return (left, right)

def createBall(left, right):
    return ball((upperXBound - lowerXBound)/2, (upperYBound - lowerYBound)/2, ballRadius, ballVelocityX, ballVelocityY, lowerXBound, upperXBound, lowerYBound, upperYBound, left, right)

def drawEverything(left, right, ball):
    screen.fill((0,0,0))
    pygame.draw.rect(screen, (255,255,255), left.rect)
    pygame.draw.rect(screen, (255,255,255), right.rect)
    pygame.draw.circle(screen, (255,255,255), (int(ball.posX), int(ball.posY)), ball.ballRadius)
    pygame.display.update()

def handleQuit():
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit()

def movePlayers(left, right):
    keys = pygame.key.get_pressed()
    if keys[pygame.K_UP]:
        right.moveUp(playerVelocity)
    if keys[pygame.K_DOWN]:
        right.moveDown(playerVelocity)
    if keys[pygame.K_w]:
        left.moveUp(playerVelocity)
    if keys[pygame.K_s]:
        left.moveDown(playerVelocity)

def runGame(left, right, ball):
    clock = pygame.time.Clock()
    while True:
        clock.tick(fps)
        handleQuit()
        movePlayers(left, right)
        winCondition = ball.move()
        if winCondition == 1:
            print("Right Player Wins!")
            pygame.quit()
            sys.exit()
        if winCondition == -1:
            print("Left Player Wins!")
            pygame.quit()
            sys.exit()
        drawEverything(left, right, ball)

if __name__ == "__main__":
    pygame.init()
    screen = pygame.display.set_mode((upperXBound, upperYBound))
    pygame.display.set_caption("Pong")
    (left, right) = createPlayers()
    ball = createBall(left, right)
    runGame(left, right, ball)
